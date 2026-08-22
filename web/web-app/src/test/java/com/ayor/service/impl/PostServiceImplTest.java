package com.ayor.service.impl;

import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.dto.PostEditDTO;
import com.ayor.entity.Base64Upload;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.PostEditHistory;
import com.ayor.entity.vo.PostEditHistoryDetailVO;
import com.ayor.entity.vo.PostEditHistoryVO;
import com.ayor.entity.vo.PostVO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostEditHistoryMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.mq.EsIndexSyncProducer;
import com.ayor.service.ForumRealtimeService;
import com.ayor.service.ImageAssetService;
import com.ayor.image.ImageStorageService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.UserRelationService;
import com.ayor.util.STOMPUtils;
import com.ayor.util.TipTapUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private MentionMessageService mentionMessageService;

    @Mock
    private ForumRealtimeService forumRealtimeService;

    @Mock
    private ImageAssetService imageAssetService;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private UserRelationService userRelationService;

    @Mock
    private PostEditHistoryMapper postEditHistoryMapper;

    @Mock
    private EsIndexSyncProducer esIndexSyncProducer;

    // 测试分页帖子按帖子串 ID
    @Test
    void shouldPagePostsByThreadId() {
        PostServiceImpl service = createService();

        Post post = new Post();
        post.setPostId(21);
        post.setThreadId(9);
        post.setAccountId(3);
        post.setTopicId(7);
        post.setContent("{\"type\":\"doc\",\"content\":[]}");
        post.setImagesUrls(List.of("https://example.com/post.png"));
        post.setIsDeleted(false);
        post.setCreateTime(new Date());

        Page<Post> page = Page.of(2, 5);
        page.setRecords(List.of(post));
        page.setTotal(12);

        Account account = new Account();
        account.setAccountId(3);
        account.setNickname("reply-user");
        account.setAvatarUrl("avatar.png");

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(false);
        when(userRelationService.listBlockedAccountIdsEitherDirection(5)).thenReturn(List.of());
        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(accountMapper.getAccountById(3)).thenReturn(account);

        PageEntity<PostVO> result = service.getPostsByThreadId(5, 9, 2, 5);

        assertEquals(12L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertEquals(21, result.getData().get(0).getPostId());
        assertEquals("reply-user", result.getData().get(0).getNickname());
        assertEquals("avatar.png", result.getData().get(0).getAvatarUrl());
        assertEquals(List.of("https://example.com/post.png"), result.getData().get(0).getImageUrls());
        assertEquals(0, result.getData().get(0).getEditCount());
        verify(postMapper).selectPage(any(Page.class), any(Wrapper.class));
    }

    // 测试分页回复返回一层被回复的帖子
    @Test
    void shouldIncludeSingleLevelReplyToPostWhenPagingPosts() {
        PostServiceImpl service = createService();

        Post post = new Post();
        post.setPostId(21);
        post.setThreadId(9);
        post.setReplyTo(20);
        post.setAccountId(3);
        post.setTopicId(7);
        post.setContent("{\"type\":\"doc\",\"content\":[]}");
        post.setImagesUrls(List.of("https://example.com/post.png"));
        post.setIsDeleted(false);
        post.setCreateTime(new Date());

        Post replyTo = new Post();
        replyTo.setPostId(20);
        replyTo.setThreadId(9);
        replyTo.setReplyTo(19);
        replyTo.setAccountId(4);
        replyTo.setTopicId(7);
        replyTo.setContent("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\"}]}");
        replyTo.setImagesUrls(List.of("https://example.com/reply-to.png"));
        replyTo.setIsDeleted(false);
        replyTo.setCreateTime(new Date());

        Page<Post> page = Page.of(1, 10);
        page.setRecords(List.of(post));
        page.setTotal(1);

        Account account = new Account();
        account.setAccountId(3);
        account.setNickname("reply-user");
        account.setAvatarUrl("avatar.png");

        Account replyToAccount = new Account();
        replyToAccount.setAccountId(4);
        replyToAccount.setNickname("target-user");
        replyToAccount.setAvatarUrl("target.png");

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(false);
        when(userRelationService.listBlockedAccountIdsEitherDirection(5)).thenReturn(List.of());
        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(postMapper.selectBatchIds(any())).thenReturn(List.of(replyTo));
        when(accountMapper.getAccountById(3)).thenReturn(account);
        when(accountMapper.getAccountById(4)).thenReturn(replyToAccount);

        PageEntity<PostVO> result = service.getPostsByThreadId(5, 9, 1, 10);

        PostVO postVO = result.getData().get(0);
        assertNotNull(postVO.getReplyTo());
        assertEquals(20, postVO.getReplyTo().getPostId());
        assertEquals("target-user", postVO.getReplyTo().getNickname());
        assertEquals("target.png", postVO.getReplyTo().getAvatarUrl());
        assertEquals(List.of("https://example.com/post.png"), postVO.getImageUrls());
        assertEquals(List.of("https://example.com/reply-to.png"), postVO.getReplyTo().getImageUrls());
        assertNull(postVO.getReplyTo().getReplyTo());
    }

    // 测试排除拉黑账号从帖子分页
    @Test
    void shouldExcludeBlockedAccountsFromPostPages() {
        PostServiceImpl service = createService();
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(false);
        when(userRelationService.listBlockedAccountIdsEitherDirection(5)).thenReturn(List.of(3));

        Page<Post> page = Page.of(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);

        service.getPostsByThreadId(5, 9, 1, 10);

        verify(postMapper).selectPage(any(Page.class), any(Wrapper.class));
    }

    // 测试拒绝帖子分页当查看者拉黑带有帖子串作者
    @Test
    void shouldDenyPostPagesWhenViewerBlockedWithThreadAuthor() {
        PostServiceImpl service = createService();
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> service.getPostsByThreadId(5, 9, 1, 10));

        verify(postMapper, never()).selectPage(any(Page.class), any(Wrapper.class));
    }

    // 测试保存帖子后同步图片引用
    @Test
    void shouldSyncImageRefsAfterSavingPost() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");
        dto.setImageUrls(List.of("https://example.com/post.png"));

        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(stompUtils.isUserSubscribed("11", "/notif/reply")).thenReturn(false);
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(123);
            return 1;
        }).when(postMapper).insert(any(Post.class));

        String result = service.insertPost(dto, 5);

        assertNull(result);
        verify(imageAssetService).syncContentRefs("POST", 123, List.of("https://example.com/post.png"), 5);
        verify(forumRealtimeService).publishPostCreated(any(Post.class));
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    // 测试创建回复时保存同帖子下的 reply_to
    @Test
    void shouldSaveReplyToWhenTargetPostBelongsToSameThread() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setReplyTo(20);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        Post replyTo = new Post();
        replyTo.setPostId(20);
        replyTo.setThreadId(9);
        replyTo.setIsDeleted(false);

        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(postMapper.selectById(20)).thenReturn(replyTo);
        when(stompUtils.isUserSubscribed("11", "/notif/reply")).thenReturn(false);
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(123);
            return 1;
        }).when(postMapper).insert(any(Post.class));

        String result = service.insertPost(dto, 5);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        assertNull(result);
        verify(postMapper).insert(postCaptor.capture());
        assertEquals(20, postCaptor.getValue().getReplyTo());
        assertEquals(9, postCaptor.getValue().getThreadId());
    }

    // 测试创建回复时拒绝不存在的 reply_to
    @Test
    void shouldRejectMissingReplyToWhenCreatingPost() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setReplyTo(88);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(postMapper.selectById(88)).thenReturn(null);

        String result = service.insertPost(dto, 5);

        assertEquals("回复对象不存在", result);
        verify(postMapper, never()).insert(any(Post.class));
    }

    // 测试创建回复时拒绝已删除的 reply_to
    @Test
    void shouldRejectDeletedReplyToWhenCreatingPost() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setReplyTo(20);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        Post replyTo = new Post();
        replyTo.setPostId(20);
        replyTo.setThreadId(9);
        replyTo.setIsDeleted(true);

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(postMapper.selectById(20)).thenReturn(replyTo);

        String result = service.insertPost(dto, 5);

        assertEquals("回复对象不存在", result);
        verify(postMapper, never()).insert(any(Post.class));
    }

    // 测试创建回复时拒绝其他帖子下的 reply_to
    @Test
    void shouldRejectReplyToFromDifferentThreadWhenCreatingPost() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setReplyTo(20);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");
        dto.setImages(List.of(new Base64Upload("data:image/png;base64,new", "new.png")));

        Post replyTo = new Post();
        replyTo.setPostId(20);
        replyTo.setThreadId(10);
        replyTo.setIsDeleted(false);

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(postMapper.selectById(20)).thenReturn(replyTo);

        String result = service.insertPost(dto, 5);

        assertEquals("回复对象不属于当前帖子", result);
        verify(postMapper, never()).insert(any(Post.class));
        verifyNoInteractions(imageStorageService);
    }

    // 测试楼主正在当前帖子详情页时不额外推送 reply 实时消息
    @Test
    void shouldNotPushReplyNotificationWhenThreadAuthorIsViewingThread() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(stompUtils.isUserSubscribed("11", "/notif/reply")).thenReturn(true);
        when(stompUtils.isUserSubscribed("11", "/broadcast/forum/threads/9/posts")).thenReturn(true);
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(123);
            return 1;
        }).when(postMapper).insert(any(Post.class));

        String result = service.insertPost(dto, 5);

        assertNull(result);
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
        verify(forumRealtimeService).publishPostCreated(any(Post.class));
    }

    // 测试楼主不在当前帖子详情页时仍推送 reply 实时消息
    @Test
    void shouldPushReplyNotificationWhenThreadAuthorIsNotViewingThread() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        Account account = new Account();
        account.setNickname("reply-user");

        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(threaddMapper.getThreadTitleById(9)).thenReturn("thread");
        when(accountMapper.getAccountById(5)).thenReturn(account);
        when(stompUtils.isUserSubscribed("11", "/notif/reply")).thenReturn(true);
        when(stompUtils.isUserSubscribed("11", "/broadcast/forum/threads/9/posts")).thenReturn(false);
        doAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setPostId(123);
            return 1;
        }).when(postMapper).insert(any(Post.class));

        String result = service.insertPost(dto, 5);

        assertNull(result);
        verify(messagingTemplate).convertAndSendToUser(eq("11"), eq("/notif/reply"), any(ReplyMessageVO.class));
    }

    // 测试拒绝新增帖子当拉黑带有帖子串作者
    @Test
    void shouldRejectInsertPostWhenBlockedWithThreadAuthor() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");
        dto.setImages(List.of(new Base64Upload("data:image/png;base64,new", "new.png")));

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(userRelationService.isBlockedEitherDirection(5, 11)).thenReturn(true);

        String result = service.insertPost(dto, 5);

        assertEquals("已拉黑，不能回复", result);
        verify(postMapper, never()).insert(any(Post.class));
        verifyNoInteractions(imageStorageService, imageAssetService, mentionMessageService, forumRealtimeService);
    }

    // 测试新增帖子失败时不推送实时事件
    @Test
    void shouldNotPublishRealtimeEventWhenInsertPostFails() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(postMapper.insert(any(Post.class))).thenReturn(0);

        String result = service.insertPost(dto, 5);

        assertEquals("发布失败, 未知异常", result);
        verify(forumRealtimeService, never()).publishPostCreated(any(Post.class));
    }

    @Test
    void shouldRejectPostImageNodeBeforePersistenceOrImageSync() {
        PostServiceImpl service = createService();
        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"https://example.com/image.png\"}}]}");

        String result = service.insertPost(dto, 5);

        assertEquals("TipTap 内容不支持图片节点，请使用 images", result);
        verify(postMapper, never()).insert(any(Post.class));
        verifyNoInteractions(imageStorageService, imageAssetService, mentionMessageService, forumRealtimeService, esIndexSyncProducer);
    }

    // 测试快照并更新帖子当编辑
    @Test
    void shouldSnapshotAndUpdatePostWhenEditing() {
        PostServiceImpl service = createService();
        Post post = createPost();
        post.setContent("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"old\"}]}]}");
        when(postMapper.selectById(21)).thenReturn(post);
        when(postEditHistoryMapper.insert(any(PostEditHistory.class))).thenReturn(1);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        PostEditDTO dto = new PostEditDTO("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"new\"}]}]}");

        String result = service.editPost(21, dto, 3);

        assertNull(result);
        ArgumentCaptor<PostEditHistory> historyCaptor = ArgumentCaptor.forClass(PostEditHistory.class);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postEditHistoryMapper).insert(historyCaptor.capture());
        verify(postMapper).updateById(postCaptor.capture());
        assertEquals(21, historyCaptor.getValue().getPostId());
        assertEquals(3, historyCaptor.getValue().getEditorAccountId());
        assertEquals("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"old\"}]}]}", historyCaptor.getValue().getContent());
        assertEquals(dto.getContent(), postCaptor.getValue().getContent());
        assertEquals(3, postCaptor.getValue().getAccountId());
        InOrder inOrder = inOrder(postEditHistoryMapper, postMapper);
        inOrder.verify(postEditHistoryMapper).insert(any(PostEditHistory.class));
        inOrder.verify(postMapper).updateById(any(Post.class));
    }

    // 测试编辑帖子后同步图片引用和提及消息
    @Test
    void shouldSyncImageRefsAndMentionsAfterEditingPost() {
        PostServiceImpl service = createService();
        Post post = createPost();
        when(postMapper.selectById(21)).thenReturn(post);
        when(postEditHistoryMapper.insert(any(PostEditHistory.class))).thenReturn(1);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        PostEditDTO dto = new PostEditDTO("{\"type\":\"doc\",\"content\":[]}");
        dto.setImageUrls(List.of("https://example.com/post.png"));

        String result = service.editPost(21, dto, 3);

        assertNull(result);
        verify(imageAssetService).syncContentRefs("POST", 21, List.of("https://example.com/post.png"), 3);
        verify(mentionMessageService).createPostMentionMessages("{\"type\":\"doc\",\"content\":[]}", 3, 21, 9);
    }

    @Test
    void shouldMergeRetainedAndUploadedPostImagesInOrder() {
        PostServiceImpl service = createService();
        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");
        dto.setImageUrls(List.of("bucket/old.png"));
        Base64Upload upload = new Base64Upload("data:image/png;base64,new", "new.png");
        dto.setImages(List.of(upload));
        when(threaddMapper.getAccountIdByThreadIdInteger(9)).thenReturn(11);
        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(imageStorageService.storeImageBase64Images(List.of(upload), "posts/9/"))
                .thenReturn(List.of("bucket/new.png"));
        doAnswer(invocation -> {
            invocation.<Post>getArgument(0).setPostId(123);
            return 1;
        }).when(postMapper).insert(any(Post.class));

        assertNull(service.insertPost(dto, 5));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(captor.capture());
        assertEquals(List.of("bucket/old.png", "bucket/new.png"), captor.getValue().getImagesUrls());
        verify(imageAssetService).syncContentRefs("POST", 123, List.of("bucket/old.png", "bucket/new.png"), 5);
    }

    @Test
    void shouldMergeRetainedAndUploadedPostImagesBeforeCreatingEditSnapshot() {
        PostServiceImpl service = createService();
        Post post = createPost();
        when(postMapper.selectById(21)).thenReturn(post);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);
        PostEditDTO dto = new PostEditDTO("{\"type\":\"doc\",\"content\":[]}");
        dto.setImageUrls(List.of("bucket/old.png"));
        Base64Upload upload = new Base64Upload("data:image/png;base64,new", "new.png");
        dto.setImages(List.of(upload));
        when(imageStorageService.storeImageBase64Images(List.of(upload), "posts/9/"))
                .thenReturn(List.of("bucket/new.png"));

        assertNull(service.editPost(21, dto, 3));

        InOrder inOrder = inOrder(imageStorageService, postEditHistoryMapper, postMapper);
        inOrder.verify(imageStorageService).storeImageBase64Images(List.of(upload), "posts/9/");
        inOrder.verify(postEditHistoryMapper).insert(any(PostEditHistory.class));
        inOrder.verify(postMapper).updateById(any(Post.class));
        verify(imageAssetService).syncContentRefs("POST", 21, List.of("bucket/old.png", "bucket/new.png"), 3);
    }

    @Test
    void shouldRejectPostImageNodeBeforeEditSideEffects() {
        PostServiceImpl service = createService();
        when(postMapper.selectById(21)).thenReturn(createPost());
        PostEditDTO dto = new PostEditDTO("{\"type\":\"doc\",\"content\":[{\"type\":\"image\"}]}");

        String result = service.editPost(21, dto, 3);

        assertEquals("TipTap 内容不支持图片节点，请使用 images", result);
        verify(postEditHistoryMapper, never()).insert(any(PostEditHistory.class));
        verify(postMapper, never()).updateById(any(Post.class));
        verifyNoInteractions(imageStorageService, imageAssetService, mentionMessageService, esIndexSyncProducer);
    }

    // 测试返回缺失帖子消息当编辑已删除帖子
    @Test
    void shouldReturnMissingPostMessageWhenEditingDeletedPost() {
        PostServiceImpl service = createService();
        Post post = createPost();
        post.setIsDeleted(true);
        when(postMapper.selectById(21)).thenReturn(post);

        String result = service.editPost(21, new PostEditDTO("{\"type\":\"doc\",\"content\":[]}"), 3);

        assertEquals("回复不存在", result);
        verify(postEditHistoryMapper, never()).insert(any(PostEditHistory.class));
        verify(imageAssetService, never()).syncContentRefs(any(), any(), any(), any());
    }

    // 测试非作者编辑帖子时透传访问拒绝
    @Test
    void shouldPropagateAccessDeniedWhenNonAuthorEditsPost() {
        PostServiceImpl service = createService();
        org.mockito.Mockito.doThrow(new AccessDeniedException("Access denied"))
                .when(authorizationService).assertCanEditPost(4, 21);

        assertThrows(AccessDeniedException.class,
                () -> service.editPost(21, new PostEditDTO("{\"type\":\"doc\",\"content\":[]}"), 4));

        verify(postMapper, never()).selectById(21);
        verify(postEditHistoryMapper, never()).insert(any(PostEditHistory.class));
    }

    // 测试统计帖子编辑次数
    @Test
    void shouldCountPostEdits() {
        PostServiceImpl service = createService();
        when(postEditHistoryMapper.selectCount(any(Wrapper.class))).thenReturn(2L);

        Integer result = service.countEdits(21);

        assertEquals(2, result);
    }

    // 测试列表公开帖子编辑历史不带内容快照
    @Test
    void shouldListPublicPostEditHistoryWithoutContentSnapshot() {
        PostServiceImpl service = createService();
        PostEditHistory history = createHistory();
        Account editor = new Account();
        editor.setAccountId(3);
        editor.setNickname("editor");
        editor.setAvatarUrl("avatar.png");
        when(postEditHistoryMapper.selectList(any(Wrapper.class))).thenReturn(List.of(history));
        when(accountMapper.getAccountById(3)).thenReturn(editor);

        List<PostEditHistoryVO> result = service.listEditHistory(21);

        assertEquals(1, result.size());
        assertEquals(21, result.get(0).getPostId());
        assertEquals("editor", result.get(0).getEditorName());
        assertEquals("avatar.png", result.get(0).getEditorAvatar());
        assertFalse(result.get(0) instanceof PostEditHistoryDetailVO);
    }

    // 测试列表帖子编辑历史快照带有内容
    @Test
    void shouldListPostEditHistorySnapshotsWithContent() {
        PostServiceImpl service = createService();
        PostEditHistory history = createHistory();
        when(postEditHistoryMapper.selectList(any(Wrapper.class))).thenReturn(List.of(history));
        when(accountMapper.getAccountById(3)).thenReturn(null);

        List<PostEditHistoryDetailVO> result = service.listEditHistoryWithSnapshots(21);

        assertEquals(1, result.size());
        assertEquals("{\"type\":\"doc\",\"content\":[]}", result.get(0).getContent());
    }

    // 测试回复消息列表使用帖子查询而不是最近帖子串查询
    @Test
    void shouldListReplyMessagesWithPostQueryInsteadOfRecentThreads() {
        PostServiceImpl service = createService();

        Post reply = new Post();
        reply.setPostId(21);
        reply.setThreadId(9);
        reply.setAccountId(3);
        reply.setTopicId(7);
        reply.setContent("{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"latest reply\"}]}]}");
        reply.setCreateTime(new Date());

        Page<Post> page = Page.of(1, 10);
        page.setRecords(List.of(reply));
        page.setTotal(1);

        Account account = new Account();
        account.setNickname("reply-user");

        when(postMapper.listReplyMessages(any(Page.class), eq(12))).thenReturn(page);
        when(threaddMapper.getThreadTitleById(9)).thenReturn("old thread");
        when(threaddMapper.getTopicIdByThreadId(9)).thenReturn(7);
        when(accountMapper.getAccountById(3)).thenReturn(account);

        PageEntity<ReplyMessageVO> result = service.listReplyMessage(1, 10, 12);

        assertEquals(1L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertEquals(21, result.getData().get(0).getPostId());
        assertEquals("old thread", result.getData().get(0).getThreadTitle());
        verify(postMapper).listReplyMessages(any(Page.class), eq(12));
        verify(threaddMapper, never()).getThreadAroundWeekById(12);
    }

    private PostServiceImpl createService() {
        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                accountMapper,
                new TipTapUtils(),
                threaddMapper,
                messagingTemplate,
                stompUtils,
                mentionMessageService,
                forumRealtimeService,
                imageAssetService,
                imageStorageService,
                authorizationService,
                userRelationService,
                postEditHistoryMapper,
                esIndexSyncProducer
        );
        ReflectionTestUtils.setField(service, "baseMapper", postMapper);
        return service;
    }

    private Post createPost() {
        Post post = new Post();
        post.setPostId(21);
        post.setThreadId(9);
        post.setAccountId(3);
        post.setTopicId(7);
        post.setContent("{\"type\":\"doc\",\"content\":[]}");
        post.setIsDeleted(false);
        post.setCreateTime(new Date());
        return post;
    }

    private PostEditHistory createHistory() {
        PostEditHistory history = new PostEditHistory();
        history.setHistoryId(51);
        history.setPostId(21);
        history.setEditorAccountId(3);
        history.setContent("{\"type\":\"doc\",\"content\":[]}");
        history.setEditTime(new Date());
        return history;
    }
}
