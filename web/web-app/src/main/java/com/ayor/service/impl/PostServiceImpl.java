package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.vo.PostVO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.ImageAssetService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.PostService;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;

    private final AccountMapper accountMapper;

    private final TipTapUtils tipTapUtils;

    private final ThreaddMapper threaddMapper;

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;

    private final MentionMessageService mentionMessageService;

    private final ImageAssetService imageAssetService;
    /**
     * 获取指定帖子下的评论列表。
     */


    @Override
    public List<PostVO> getPostsByThreadId(Integer threadId) {
        if (threadId == null ) {
            return null;
        }
        List<Post> posts = postMapper.getPostsByThreadId(threadId);
        List<PostVO> postVOList = new ArrayList<>();
        posts.forEach(post -> {
            PostVO postVO = new PostVO();
            if (!post.getIsDeleted()) {
                BeanUtils.copyProperties(post, postVO);
                Account account = accountMapper.getAccountById(post.getAccountId());
                postVO.setNickname(account.getNickname());
                postVO.setAccountId(account.getAccountId());
                postVO.setAvatarUrl(account.getAvatarUrl());
                postVOList.add(postVO);
            }
        });
        return postVOList;
    }

    @Override
    @MessageUnreadNotif(
            accountId = "@threaddMapper.getAccountIdByThreadIdInteger(#postDTO.threadId)",
            subscribeDest = "/notif/reply",
            type = UnreadMessageType.REPLY_MESSAGE)
    /**
     * 新增评论并处理相关通知与索引。
     */
    public String insertPost(PostDTO postDTO, Integer userId) {
        if (postDTO.getContent() == null) {
            return "请填写内容";
        }
        if (postDTO.getThreadId() == null) {
            return "未知的发送";
        }
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post);
        if (userId == null) {
            return "用户不存在";
        }
        Integer topicId = threaddMapper.getTopicIdByThreadId(postDTO.getThreadId());
        post.setAccountId(userId)   ;
        try {
            post.setContent(tipTapUtils.convertBase64ImagesToUrl(postDTO.getContent(), "posts/" + postDTO.getThreadId() + "/"));
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
        post.setCreateTime(new Date());
        post.setTopicId(topicId);
        if (this.save(post)) {
            imageAssetService.syncContentRefs("POST", post.getPostId(), post.getContent(), userId);
            Integer accountId = threaddMapper.getAccountIdByThreadIdInteger(post.getThreadId());
            // TODO 自己在自己的帖子下面回复不要通知
            if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/reply")) {
                messagingTemplate.convertAndSendToUser(
                        threaddMapper.getAccountIdByThreadIdInteger(postDTO.getThreadId()).toString(),
                        "/notif/reply",
                        toVO(post)
                );
            }
            mentionMessageService.createPostMentionMessages(post.getContent(), userId, post.getPostId(), post.getThreadId());
            return null;
        }
        return "发布失败, 未知异常";
    }
    /**
     * 校验作者身份后删除评论。
     */

    @Override
    public String removePostAuthorizeAccountId(Integer postId, Integer userId) {
        Post post = this.getById(postId);
        if (post == null) {
            return "帖子不存在";
        }
        if (!post.getAccountId().equals(userId)) {
            return "没有权限";
        }
        imageAssetService.clearContentRefs("POST", postId);
        return this.removeByIdLogic(post.getPostId()) ? null : "删除失败, 未知异常";
    }
    /**
     * 管理员直接删除评论。
     */

    @Override
    public String removePostPermission(Integer postId) {
        Post post = this.getById(postId);
        if (post == null) {
            return "帖子不存在";
        }
        imageAssetService.clearContentRefs("POST", postId);
        return this.removeByIdLogic(post.getPostId()) ? null : "删除失败, 未知异常";
    }

    /**
     * 分页获取回复消息列表。
     */
    @Override
    @MessageUnreadNotif(
            accountId = "#accountId",
            subscribeDest = "/notif/reply",
            type = UnreadMessageType.REPLY_MESSAGE,
            doRead = true
    )
    public PageEntity<ReplyMessageVO> listReplyMessage(Integer pageNum, Integer pageSize, Integer accountId) {
        if (accountId == null) return new PageEntity<>(0L, Collections.emptyList());
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        List<Threadd> threads = threaddMapper.getThreadAroundWeekById(accountId);

        if (threads == null || threads.isEmpty()) {
            return new PageEntity<>(0L, Collections.emptyList());
        }

        List<Integer> threadIds = threads.stream()
                .map(Threadd::getThreadId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (threadIds.isEmpty()) {
            return new PageEntity<>(0L, Collections.emptyList());
        }

        Page<Post> page = this.lambdaQuery()
                .in(Post::getThreadId, threadIds)
                .orderByDesc(Post::getCreateTime)
                .page(Page.of(pageNum, pageSize));  // 注意：页码从 1 开始

        List<ReplyMessageVO> vos = toVOList(page.getRecords());
        return new PageEntity<>(page.getTotal(), vos);
    }
    /**
     * 将评论实体转换为搜索索引文档。
     */


    @Override
    public List<ThreadDoc> toThreadDoc(List<Post> posts) {
        Map<Integer, Threadd> threadMap = new HashMap<>();

        List<ThreadDoc> threadDocs = new ArrayList<>();
        posts.forEach(post -> {
            if (!threadMap.containsKey(post.getThreadId())) {
                Threadd thread = threaddMapper.selectById(post.getThreadId());
                threadMap.put(post.getThreadId(), thread);
            }
            Threadd thread = threadMap.get(post.getThreadId());
            ThreadDoc threadDoc = new ThreadDoc();
            BeanUtils.copyProperties(thread, threadDoc);
            threadDoc.setContent(tipTapUtils.extractText(post.getContent()));
            threadDoc.setCreateTime(post.getCreateTime());
            threadDoc.setUpdateTime(post.getUpdateTime());
            threadDoc.setId("POST-" + post.getPostId());
            threadDoc.setIsThreadTopic(false);
            threadDocs.add(threadDoc);
        });
        return threadDocs;
    }
    /**
     * 将评论实体列表转换为回复视图对象列表。
     */

    private List<ReplyMessageVO> toVOList(List<Post> posts) {
        List<ReplyMessageVO> vos = new ArrayList<>();
        posts.forEach(post -> {
            ReplyMessageVO vo = toVO(post);
            vos.add(vo);
        });
        return vos;
    }
    /**
     * 将单条评论实体转换为回复视图对象。
     */

    @NotNull
    private ReplyMessageVO toVO(Post post) {
        ReplyMessageVO vo = new ReplyMessageVO();
        vo.setPostId(post.getPostId());
        vo.setThreadId(post.getThreadId());
        vo.setThreadTitle(threaddMapper.getThreadTitleById(post.getThreadId()));
        vo.setContent(tipTapUtils.filterNonImage(post.getContent()));
        vo.setTopicId(threaddMapper.getTopicIdByThreadId(post.getThreadId()));
        vo.setCreateTime(post.getCreateTime());
        vo.setNickname(accountMapper.getAccountById(post.getAccountId()).getNickname());
        return vo;
    }
    /**
     * 将评论标记为已删除。
     */


    private boolean removeByIdLogic(Serializable Id) {
        Post post = this.getById(Id);
        if (post == null) {
            return false;
        }
        post.setIsDeleted(true);
        return this.updateById(post);
    }

}
