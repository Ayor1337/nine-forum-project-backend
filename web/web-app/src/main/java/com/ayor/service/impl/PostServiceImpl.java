package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.entity.dto.PostEditDTO;
import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.pojo.PostEditHistory;
import com.ayor.entity.vo.PostEditHistoryDetailVO;
import com.ayor.entity.vo.PostEditHistoryVO;
import com.ayor.entity.vo.PostVO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostEditHistoryMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.mq.EsIndexSyncProducer;
import com.ayor.service.ForumRealtimeService;
import com.ayor.service.ImageAssetService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.PostService;
import com.ayor.service.UserRelationService;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import com.ayor.image.ImageStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;

/**
 * 评论服务实现
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private static final String REPLY_NOTIFICATION_DESTINATION = "/notif/reply";
    private static final String THREAD_POSTS_REALTIME_DESTINATION = "/broadcast/forum/threads/%d/posts";

    private final PostMapper postMapper;

    private final AccountMapper accountMapper;

    private final TipTapUtils tipTapUtils;

    private final ThreaddMapper threaddMapper;

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;

    private final MentionMessageService mentionMessageService;

    private final ForumRealtimeService forumRealtimeService;

    private final ImageAssetService imageAssetService;

    private final ImageStorageService imageStorageService;

    private final AuthorizationService authorizationService;

    private final UserRelationService userRelationService;

    private final PostEditHistoryMapper postEditHistoryMapper;

    private final EsIndexSyncProducer esIndexSyncProducer;
    /**
     * 获取指定帖子下的评论列表。
     */


    @Override
    public PageEntity<PostVO> getPostsByThreadId(Integer viewerId, Integer threadId, Integer pageNum, Integer pageSize) {
        if (threadId == null) {
            return new PageEntity<>(0L, Collections.emptyList());
        }
        Integer threadAuthorId = threaddMapper.getAccountIdByThreadIdInteger(threadId);
        if (threadAuthorId == null) {
            return new PageEntity<>(0L, Collections.emptyList());
        }
        if (viewerId != null && !Objects.equals(viewerId, threadAuthorId)
                && userRelationService.isBlockedEitherDirection(viewerId, threadAuthorId)) {
            throw new AccessDeniedException("Access denied");
        }
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Page<Post> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getThreadId, threadId)
                .eq(Post::getIsDeleted, false)
                .orderByAsc(Post::getCreateTime);
        applyBlockedAuthorFilter(wrapper, viewerId);
        Page<Post> posts = this.baseMapper.selectPage(page, wrapper);
        return new PageEntity<>(posts.getTotal(), toPostVOs(posts.getRecords()));
    }

    private void applyBlockedAuthorFilter(LambdaQueryWrapper<Post> wrapper, Integer viewerId) {
        if (viewerId == null) {
            return;
        }
        List<Integer> blockedAccountIds = userRelationService.listBlockedAccountIdsEitherDirection(viewerId);
        if (!blockedAccountIds.isEmpty()) {
            wrapper.notIn(Post::getAccountId, blockedAccountIds);
        }
    }

    private List<PostVO> toPostVOs(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return new ArrayList<>();
        }
        List<PostVO> postVOList = new ArrayList<>();
        Map<Integer, PostVO> postVOMap = new HashMap<>();
        Map<Integer, Integer> postReplyToMap = new HashMap<>();
        Set<Integer> replyToIds = new HashSet<>();
        posts.forEach(post -> {
            PostVO postVO = toPostVO(post);
            postVOList.add(postVO);
            postVOMap.put(post.getPostId(), postVO);
            postReplyToMap.put(post.getPostId(), post.getReplyTo());
            if (post.getReplyTo() != null) {
                replyToIds.add(post.getReplyTo());
            }
        });
        if (replyToIds.isEmpty()) {
            return postVOList;
        }
        List<Post> replyToPosts = this.baseMapper.selectBatchIds(replyToIds);
        Map<Integer, PostVO> replyToVOMap = new HashMap<>();
        replyToPosts.forEach(replyToPost -> {
            if (!Boolean.TRUE.equals(replyToPost.getIsDeleted())) {
                PostVO replyToVO = toPostVO(replyToPost);
                replyToVO.setReplyTo(null);
                replyToVOMap.put(replyToPost.getPostId(), replyToVO);
            }
        });
        postVOList.forEach(postVO -> {
            Integer replyToId = postReplyToMap.get(postVO.getPostId());
            postVO.setReplyTo(replyToVOMap.get(replyToId));
            PostVO samePageVO = postVOMap.get(replyToId);
            if (samePageVO != null) {
                PostVO replyToVO = new PostVO();
                BeanUtils.copyProperties(samePageVO, replyToVO);
                replyToVO.setReplyTo(null);
                postVO.setReplyTo(replyToVO);
            }
        });
        return postVOList;
    }

    private PostVO toPostVO(Post post) {
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setImageUrls(normalizeImageUrls(post.getImagesUrls()));
        Account account = accountMapper.getAccountById(post.getAccountId());
        postVO.setNickname(account.getNickname());
        postVO.setAccountId(account.getAccountId());
        postVO.setAvatarUrl(account.getAvatarUrl());
        postVO.setEditCount(countEdits(post.getPostId()));
        return postVO;
    }

    @Override
    @MessageUnreadNotif(
            accountId = "@threaddMapper.getAccountIdByThreadIdInteger(#postDTO.threadId)",
            subscribeDest = "/notif/reply",
            type = UnreadMessageType.REPLY_MESSAGE)
    /**
     * 新增评论并处理相关通知与索引异步同步。
     */
    public String insertPost(PostDTO postDTO, Integer userId) {
        if (postDTO.getContent() == null) {
            return "请填写内容";
        }
        if (postDTO.getThreadId() == null) {
            return "未知的发送";
        }
        String contentError = validatePostContent(postDTO.getContent());
        if (contentError != null) {
            return contentError;
        }
        Post post = new Post();
        BeanUtils.copyProperties(postDTO, post);
        if (userId == null) {
            return "用户不存在";
        }
        Integer threadAuthorId = threaddMapper.getAccountIdByThreadIdInteger(postDTO.getThreadId());
        if (threadAuthorId == null) {
            return "帖子不存在";
        }
        if (!Objects.equals(userId, threadAuthorId)
                && userRelationService.isBlockedEitherDirection(userId, threadAuthorId)) {
            return "已拉黑，不能回复";
        }
        String replyToError = validateReplyTo(postDTO);
        if (replyToError != null) {
            return replyToError;
        }
        Integer topicId = threaddMapper.getTopicIdByThreadId(postDTO.getThreadId());
        post.setAccountId(userId)   ;
        post.setContent(postDTO.getContent());
        post.setImagesUrls(mergeImageUrls(postDTO.getImageUrls(), postDTO.getImages(), postImagePath(postDTO.getThreadId())));
        post.setCreateTime(new Date());
        post.setTopicId(topicId);
        if (this.save(post)) {
            imageAssetService.syncContentRefs("POST", post.getPostId(), post.getImagesUrls(), userId);
            Integer currentPostAccountId = threadAuthorId;
            if (shouldPushReplyNotification(currentPostAccountId, userId, post.getThreadId())) {
                messagingTemplate.convertAndSendToUser(
                        threaddMapper.getAccountIdByThreadIdInteger(postDTO.getThreadId()).toString(),
                        REPLY_NOTIFICATION_DESTINATION,
                        toVO(post)
                );
            }
            mentionMessageService.createPostMentionMessages(post.getContent(), userId, post.getPostId(), post.getThreadId());
            forumRealtimeService.publishPostCreated(post);
            esIndexSyncProducer.syncPost(post.getPostId());
            return null;
        }
        return "发布失败, 未知异常";
    }

    private String validateReplyTo(PostDTO postDTO) {
        if (postDTO.getReplyTo() == null) {
            return null;
        }
        Post replyTo = this.getById(postDTO.getReplyTo());
        if (replyTo == null || Boolean.TRUE.equals(replyTo.getIsDeleted())) {
            return "回复对象不存在";
        }
        if (!Objects.equals(replyTo.getThreadId(), postDTO.getThreadId())) {
            return "回复对象不属于当前帖子";
        }
        return null;
    }

    private String validatePostContent(String content) {
        try {
            tipTapUtils.assertNoImageNodes(content);
            return null;
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        return imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    private List<Base64Upload> normalizeImages(List<Base64Upload> images) {
        return images == null ? new ArrayList<>() : new ArrayList<>(images);
    }

    private List<String> mergeImageUrls(List<String> imageUrls, List<Base64Upload> images, String path) {
        List<String> mergedImageUrls = normalizeImageUrls(imageUrls);
        mergedImageUrls.addAll(imageStorageService.storeImageBase64Images(normalizeImages(images), path));
        return mergedImageUrls;
    }

    private String postImagePath(Integer threadId) {
        return "posts/" + threadId + "/";
    }

    private boolean shouldPushReplyNotification(Integer threadAuthorId, Integer senderId, Integer threadId) {
        if (threadAuthorId == null || threadAuthorId.equals(senderId)) {
            return false;
        }
        String authorId = threadAuthorId.toString();
        if (!stompUtils.isUserSubscribed(authorId, REPLY_NOTIFICATION_DESTINATION)) {
            return false;
        }
        return !stompUtils.isUserSubscribed(authorId, THREAD_POSTS_REALTIME_DESTINATION.formatted(threadId));
    }

    /**
     * 编辑回复：先快照原正文，再以新正文覆盖当前回复。
     */
    @Override
    public String editPost(Integer postId, PostEditDTO postEditDTO, Integer accountId) {
        if (postId == null || postEditDTO == null) {
            return "参数错误";
        }
        authorizationService.assertCanEditPost(accountId, postId);
        Post post = this.getById(postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return "回复不存在";
        }

        String contentError = validatePostContent(postEditDTO.getContent());
        if (contentError != null) {
            return contentError;
        }
        String newContent = postEditDTO.getContent();
        List<String> imageUrls = mergeImageUrls(postEditDTO.getImageUrls(), postEditDTO.getImages(), postImagePath(post.getThreadId()));

        PostEditHistory snapshot = new PostEditHistory();
        snapshot.setPostId(postId);
        snapshot.setEditorAccountId(accountId);
        snapshot.setContent(post.getContent());
        snapshot.setEditTime(new Date());
        postEditHistoryMapper.insert(snapshot);

        post.setContent(newContent);
        post.setImagesUrls(imageUrls);
        post.setUpdateTime(new Date());
        if (!this.updateById(post)) {
            return "编辑失败";
        }

        imageAssetService.syncContentRefs("POST", postId, post.getImagesUrls(), accountId);
        mentionMessageService.createPostMentionMessages(newContent, accountId, postId, post.getThreadId());
        esIndexSyncProducer.syncPost(postId);
        return null;
    }

    @Override
    public Integer countEdits(Integer postId) {
        if (postId == null) {
            return 0;
        }
        Long count = postEditHistoryMapper.selectCount(
                new LambdaQueryWrapper<PostEditHistory>()
                        .eq(PostEditHistory::getPostId, postId));
        return count == null ? 0 : count.intValue();
    }

    @Override
    public List<PostEditHistoryVO> listEditHistory(Integer postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        List<PostEditHistory> records = postEditHistoryMapper.selectList(
                new LambdaQueryWrapper<PostEditHistory>()
                        .eq(PostEditHistory::getPostId, postId)
                        .orderByDesc(PostEditHistory::getEditTime));
        List<PostEditHistoryVO> result = new ArrayList<>(records.size());
        for (PostEditHistory record : records) {
            PostEditHistoryVO vo = new PostEditHistoryVO();
            populateBaseHistoryVO(vo, record);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<PostEditHistoryDetailVO> listEditHistoryWithSnapshots(Integer postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        List<PostEditHistory> records = postEditHistoryMapper.selectList(
                new LambdaQueryWrapper<PostEditHistory>()
                        .eq(PostEditHistory::getPostId, postId)
                        .orderByDesc(PostEditHistory::getEditTime));
        List<PostEditHistoryDetailVO> result = new ArrayList<>(records.size());
        for (PostEditHistory record : records) {
            PostEditHistoryDetailVO vo = new PostEditHistoryDetailVO();
            populateBaseHistoryVO(vo, record);
            vo.setContent(record.getContent());
            result.add(vo);
        }
        return result;
    }

    private void populateBaseHistoryVO(PostEditHistoryVO vo, PostEditHistory record) {
        vo.setHistoryId(record.getHistoryId());
        vo.setPostId(record.getPostId());
        vo.setEditTime(record.getEditTime());
        vo.setEditorId(record.getEditorAccountId());
        Account editor = accountMapper.getAccountById(record.getEditorAccountId());
        if (editor != null) {
            vo.setEditorName(editor.getNickname());
            vo.setEditorAvatar(editor.getAvatarUrl());
        }
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
        authorizationService.assertCanDeletePost(userId, postId);
        imageAssetService.clearContentRefs("POST", postId);
        if (!this.removeByIdLogic(post.getPostId())) {
            return "删除失败, 未知异常";
        }
        esIndexSyncProducer.syncPost(postId);
        return null;
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
        if (!this.removeByIdLogic(post.getPostId())) {
            return "删除失败, 未知异常";
        }
        esIndexSyncProducer.syncPost(postId);
        return null;
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
        Page<Post> page = postMapper.listReplyMessages(Page.of(pageNum, pageSize), accountId);

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
            threadDoc.setAccountId(post.getAccountId());
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
        vo.setContent(tipTapUtils.filterStickerNodes(post.getContent()));
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
