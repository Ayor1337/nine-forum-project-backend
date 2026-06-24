package com.ayor.service.impl;

import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.stomp.PostCreatedMessage;
import com.ayor.entity.stomp.ThreadCreatedMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ForumRealtimeServiceImplTest {

    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

    private final ForumRealtimeServiceImpl service = new ForumRealtimeServiceImpl(messagingTemplate);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // 测试新增帖子串事件发送到话题目的地
    @Test
    void shouldPublishThreadCreatedEventToTopicDestination() {
        Threadd thread = new Threadd();
        Date createTime = new Date();
        thread.setTopicId(7);
        thread.setThreadId(21);
        thread.setCreateTime(createTime);

        service.publishThreadCreated(thread);

        ArgumentCaptor<ThreadCreatedMessage> captor = ArgumentCaptor.forClass(ThreadCreatedMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/broadcast/forum/topics/7/threads"), captor.capture());
        assertEquals(7, captor.getValue().getTopicId());
        assertEquals(21, captor.getValue().getThreadId());
        assertEquals(1, captor.getValue().getIncrement());
        assertSame(createTime, captor.getValue().getCreateTime());
    }

    // 测试新增回复事件发送到帖子串目的地
    @Test
    void shouldPublishPostCreatedEventToThreadDestination() {
        Post post = new Post();
        Date createTime = new Date();
        post.setTopicId(7);
        post.setThreadId(21);
        post.setPostId(33);
        post.setCreateTime(createTime);

        service.publishPostCreated(post);

        ArgumentCaptor<PostCreatedMessage> captor = ArgumentCaptor.forClass(PostCreatedMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/broadcast/forum/threads/21/posts"), captor.capture());
        assertEquals(7, captor.getValue().getTopicId());
        assertEquals(21, captor.getValue().getThreadId());
        assertEquals(33, captor.getValue().getPostId());
        assertEquals(1, captor.getValue().getIncrement());
        assertSame(createTime, captor.getValue().getCreateTime());
    }

    // 测试事务中注册提交后推送
    @Test
    void shouldPublishAfterTransactionCommitWhenSynchronizationIsActive() {
        TransactionSynchronizationManager.initSynchronization();
        Threadd thread = new Threadd();
        thread.setTopicId(7);
        thread.setThreadId(21);
        thread.setCreateTime(new Date());

        service.publishThreadCreated(thread);

        verify(messagingTemplate, never()).convertAndSend(eq("/broadcast/forum/topics/7/threads"), org.mockito.ArgumentMatchers.any(Object.class));
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }

        verify(messagingTemplate).convertAndSend(eq("/broadcast/forum/topics/7/threads"), org.mockito.ArgumentMatchers.any(Object.class));
    }
}
