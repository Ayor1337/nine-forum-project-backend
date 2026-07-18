package com.ayor.service;

import com.ayor.entity.message.EsIndexSyncMessage;
import com.ayor.type.EsIndexEntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ES 索引同步消息生产者：事务提交后发送, 保证消费端回源能查到最新数据。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EsIndexSyncProducer {

    public static final String EXCHANGE = "es.index.direct";

    public static final String ROUTING_KEY = "es.index.sync";

    private final RabbitTemplate rabbitTemplate;

    public void syncThread(Integer threadId) {
        sendAfterCommit(new EsIndexSyncMessage(EsIndexEntityType.THREAD, threadId));
    }

    public void syncPost(Integer postId) {
        sendAfterCommit(new EsIndexSyncMessage(EsIndexEntityType.POST, postId));
    }

    public void rebuildAll() {
        sendAfterCommit(new EsIndexSyncMessage(EsIndexEntityType.ALL, null));
    }

    private void sendAfterCommit(EsIndexSyncMessage message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doSend(message);
                }
            });
            return;
        }
        doSend(message);
    }

    private void doSend(EsIndexSyncMessage message) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("Elastic | 索引同步消息发送失败: {}, 可通过全量重建补偿", message, e);
        }
    }
}
