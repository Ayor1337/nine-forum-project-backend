package com.ayor.service;

import com.ayor.entity.message.EsIndexSyncMessage;
import com.ayor.type.EsIndexEntityType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EsIndexSyncProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // 测试无事务环境立即发送消息
    @Test
    void shouldSendImmediatelyWithoutTransaction() {
        EsIndexSyncProducer producer = new EsIndexSyncProducer(rabbitTemplate);

        producer.syncThread(1);

        ArgumentCaptor<EsIndexSyncMessage> captor = ArgumentCaptor.forClass(EsIndexSyncMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(EsIndexSyncProducer.EXCHANGE),
                eq(EsIndexSyncProducer.ROUTING_KEY),
                captor.capture());
        assertEquals(EsIndexEntityType.THREAD, captor.getValue().getEntityType());
        assertEquals(1, captor.getValue().getEntityId());
    }

    // 测试事务环境延迟到提交后发送消息
    @Test
    void shouldDeferSendUntilAfterCommitInTransaction() {
        EsIndexSyncProducer producer = new EsIndexSyncProducer(rabbitTemplate);
        TransactionSynchronizationManager.initSynchronization();

        producer.syncPost(2);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);

        ArgumentCaptor<EsIndexSyncMessage> captor = ArgumentCaptor.forClass(EsIndexSyncMessage.class);
        verify(rabbitTemplate).convertAndSend(
                eq(EsIndexSyncProducer.EXCHANGE),
                eq(EsIndexSyncProducer.ROUTING_KEY),
                captor.capture());
        assertEquals(EsIndexEntityType.POST, captor.getValue().getEntityType());
        assertEquals(2, captor.getValue().getEntityId());
    }

    // 测试发送异常被吞掉不影响主流程
    @Test
    void shouldSwallowSendFailure() {
        EsIndexSyncProducer producer = new EsIndexSyncProducer(rabbitTemplate);
        doThrow(new RuntimeException("mq down")).when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(Object.class));

        assertDoesNotThrow(() -> producer.syncThread(1));
    }
}
