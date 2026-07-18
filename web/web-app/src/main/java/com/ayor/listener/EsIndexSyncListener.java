package com.ayor.listener;

import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.message.EsIndexSyncMessage;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.service.ESIndexService;
import com.ayor.service.PostService;
import com.ayor.service.ThreaddService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ES 索引同步消费者：以 MySQL 当前状态为准 upsert 或删除对应文档, 保证幂等且免疫消息乱序。
 */
@RequiredArgsConstructor
@RabbitListener(queues = "es.index.queue")
@Component
@Slf4j
public class EsIndexSyncListener {

    private final ThreaddService threaddService;

    private final PostService postService;

    private final ThreaddRepository threaddRepository;

    private final ESIndexService esIndexService;

    @RabbitHandler
    public void onMessage(EsIndexSyncMessage syncMessage,
                          Message message,
                          Channel channel) {
        try {
            if (message != null && syncMessage != null && syncMessage.getEntityType() != null) {
                switch (syncMessage.getEntityType()) {
                    case THREAD -> {
                        if (syncMessage.getEntityId() != null) {
                            syncThread(syncMessage.getEntityId());
                        }
                    }
                    case POST -> {
                        if (syncMessage.getEntityId() != null) {
                            syncPost(syncMessage.getEntityId());
                        }
                    }
                    case ALL -> rebuildAll();
                }
            }
            if (message != null) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            log.error("Elastic | 索引同步消费失败: {}, 可通过全量重建补偿", syncMessage, e);
            try {
                if (message != null) {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                }
            } catch (Exception nackException) {
                log.error("Elastic | 拒绝索引同步消息失败", nackException);
            }
        }
    }

    private void rebuildAll() {
        esIndexService.initIndex();
    }

    private void syncThread(Integer threadId) {
        Threadd thread = threaddService.getById(threadId);
        if (thread == null || Boolean.TRUE.equals(thread.getIsDeleted())) {
            threaddRepository.deleteByThreadId(threadId);
            return;
        }
        threaddRepository.saveAll(threaddService.toThreadDocs(List.of(thread)));
    }

    private void syncPost(Integer postId) {
        Post post = postService.getById(postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            threaddRepository.deleteById("POST-" + postId);
            return;
        }
        Threadd thread = threaddService.getById(post.getThreadId());
        if (thread == null || Boolean.TRUE.equals(thread.getIsDeleted())) {
            threaddRepository.deleteById("POST-" + postId);
            return;
        }
        threaddRepository.saveAll(postService.toThreadDoc(List.of(post)));
    }
}
