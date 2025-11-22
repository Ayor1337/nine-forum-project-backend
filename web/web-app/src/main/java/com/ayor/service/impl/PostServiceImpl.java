package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.app.vo.ReplyMessageVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.PostService;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.QuillUtils;
import com.ayor.util.STOMPUtils;
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

    private final QuillUtils quillUtils;

    private final ThreaddMapper threaddMapper;

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;


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
        post.setContent(quillUtils.QuillDeltaConvertBase64ToURL(postDTO.getContent(), "posts/" + post.getThreadId() + "/"));
        post.setCreateTime(new Date());
        post.setTopicId(topicId);
        if (this.save(post)) {
            Integer accountId = threaddMapper.getAccountIdByThreadIdInteger(post.getThreadId());
            if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/reply")) {
                messagingTemplate.convertAndSendToUser(
                        threaddMapper.getAccountIdByThreadIdInteger(postDTO.getThreadId()).toString(),
                        "/notif/reply",
                        toVO(post)
                );
            }
            return null;
        }
        return "发布失败, 未知异常";
    }

    @Override
    public String removePostAuthorizeAccountId(Integer postId, Integer userId) {
        Post post = this.getById(postId);
        if (post == null) {
            return "帖子不存在";
        }
        if (!post.getAccountId().equals(userId)) {
            return "没有权限";
        }
        return this.removeByIdLogic(post.getPostId()) ? null : "删除失败, 未知异常";
    }

    @Override
    public String removePostPermission(Integer postId) {
        Post post = this.getById(postId);
        if (post == null) {
            return "帖子不存在";
        }
        return this.removeByIdLogic(post.getPostId()) ? null : "删除失败, 未知异常";
    }

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
            threadDoc.setContent(quillUtils.QuillStringToString(post.getContent()));
            threadDoc.setCreateTime(post.getCreateTime());
            threadDoc.setUpdateTime(post.getUpdateTime());
            threadDoc.setId("POST-" + post.getPostId());
            threadDoc.setIsThreadTopic(false);
            threadDocs.add(threadDoc);
        });
        return threadDocs;
    }

    private List<ReplyMessageVO> toVOList(List<Post> posts) {
        List<ReplyMessageVO> vos = new ArrayList<>();
        posts.forEach(post -> {
            ReplyMessageVO vo = toVO(post);
            vos.add(vo);
        });
        return vos;
    }

    @NotNull
    private ReplyMessageVO toVO(Post post) {
        ReplyMessageVO vo = new ReplyMessageVO();
        vo.setPostId(post.getPostId());
        vo.setThreadId(post.getThreadId());
        vo.setThreadTitle(threaddMapper.getThreadTitleById(post.getThreadId()));
        vo.setContent(quillUtils.QuillDeltaFilterNonImage(post.getContent()));
        vo.setTopicId(threaddMapper.getTopicIdByThreadId(post.getThreadId()));
        vo.setCreateTime(post.getCreateTime());
        vo.setNickname(accountMapper.getAccountById(post.getAccountId()).getNickname());
        return vo;
    }


    private boolean removeByIdLogic(Serializable Id) {
        Post post = this.getById(Id);
        if (post == null) {
            return false;
        }
        post.setIsDeleted(true);
        return this.updateById(post);
    }

}
