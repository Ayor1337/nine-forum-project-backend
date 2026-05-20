package com.ayor.service.impl;

import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.vo.PostVO;
import com.ayor.entity.vo.ReplyMessageVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ImageAssetService;
import com.ayor.service.MentionMessageService;
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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ImageAssetService imageAssetService;

    @Mock
    private AuthorizationService authorizationService;

    @Test
    void shouldPagePostsByThreadId() {
        PostServiceImpl service = createService();

        Post post = new Post();
        post.setPostId(21);
        post.setThreadId(9);
        post.setAccountId(3);
        post.setTopicId(7);
        post.setContent("{\"type\":\"doc\",\"content\":[]}");
        post.setIsDeleted(false);
        post.setCreateTime(new Date());

        Page<Post> page = Page.of(2, 5);
        page.setRecords(List.of(post));
        page.setTotal(12);

        Account account = new Account();
        account.setAccountId(3);
        account.setNickname("reply-user");
        account.setAvatarUrl("avatar.png");

        when(postMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(page);
        when(accountMapper.getAccountById(3)).thenReturn(account);

        PageEntity<PostVO> result = service.getPostsByThreadId(9, 2, 5);

        assertEquals(12L, result.getTotalSize());
        assertEquals(1, result.getData().size());
        assertEquals(21, result.getData().get(0).getPostId());
        assertEquals("reply-user", result.getData().get(0).getNickname());
        assertEquals("avatar.png", result.getData().get(0).getAvatarUrl());
        verify(postMapper).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    void shouldSyncImageRefsAfterSavingPost() {
        PostServiceImpl service = createService();

        PostDTO dto = new PostDTO();
        dto.setThreadId(9);
        dto.setContent("{\"type\":\"doc\",\"content\":[]}");

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
        verify(imageAssetService).syncContentRefs("POST", 123, "{\"type\":\"doc\",\"content\":[]}", 5);
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

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
                imageAssetService,
                authorizationService
        );
        ReflectionTestUtils.setField(service, "baseMapper", postMapper);
        return service;
    }
}
