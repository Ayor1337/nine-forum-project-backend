package com.ayor.service.impl;

import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.stomp.PostCreatedMessage;
import com.ayor.entity.stomp.ThreadCreatedMessage;
import com.ayor.service.ForumRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumRealtimeServiceImpl implements ForumRealtimeService {

    private static final int INCREMENT = 1;
    private static final String THREAD_DESTINATION = "/broadcast/forum/topics/%d/threads";
    private static final String POST_DESTINATION = "/broadcast/forum/threads/%d/posts";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publishThreadCreated(Threadd thread) {
        if (thread == null || thread.getTopicId() == null || thread.getThreadId() == null) {
            return;
        }
        runAfterCommit(() -> sendThreadCreated(thread));
    }

    @Override
    public void publishPostCreated(Post post) {
        if (post == null || post.getThreadId() == null || post.getPostId() == null) {
            return;
        }
        runAfterCommit(() -> sendPostCreated(post));
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }

    private void sendThreadCreated(Threadd thread) {
        try {
            ThreadCreatedMessage message = new ThreadCreatedMessage(
                    thread.getTopicId(),
                    thread.getThreadId(),
                    INCREMENT,
                    thread.getCreateTime()
            );
            messagingTemplate.convertAndSend(THREAD_DESTINATION.formatted(thread.getTopicId()), message);
        } catch (RuntimeException exception) {
            log.warn("推送新增帖子串实时事件失败: threadId={}", thread.getThreadId(), exception);
        }
    }

    private void sendPostCreated(Post post) {
        try {
            PostCreatedMessage message = new PostCreatedMessage(
                    post.getTopicId(),
                    post.getThreadId(),
                    post.getPostId(),
                    INCREMENT,
                    post.getCreateTime()
            );
            messagingTemplate.convertAndSend(POST_DESTINATION.formatted(post.getThreadId()), message);
        } catch (RuntimeException exception) {
            log.warn("推送新增回复实时事件失败: postId={}", post.getPostId(), exception);
        }
    }
}
