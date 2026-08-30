package com.ayor.listener;

import com.ayor.dao.ThreaddRepository;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.entity.message.EsIndexSyncMessage;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Threadd;
import com.ayor.service.ESIndexService;
import com.ayor.service.PostService;
import com.ayor.service.ThreaddService;
import com.ayor.type.EsIndexEntityType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EsIndexSyncListenerTest {

    @Mock
    private ThreaddService threaddService;

    @Mock
    private PostService postService;

    @Mock
    private ThreaddRepository threaddRepository;

    @Mock
    private ESIndexService esIndexService;

    @Mock
    private Channel channel;

    private EsIndexSyncListener createListener() {
        return new EsIndexSyncListener(threaddService, postService, threaddRepository, esIndexService);
    }

    private Message createMessage() {
        return new Message("{}".getBytes(), new MessageProperties());
    }

    // 测试有效帖子消息触发文档 upsert
    @Test
    void shouldUpsertWhenThreadIsAlive() throws Exception {
        EsIndexSyncListener listener = createListener();
        Threadd thread = new Threadd();
        thread.setThreadId(1);
        thread.setIsDeleted(false);
        when(threaddService.getById(1)).thenReturn(thread);
        when(threaddService.toThreadDocs(List.of(thread))).thenReturn(List.of(new ThreadDoc()));

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.THREAD, 1), createMessage(), channel);

        verify(threaddRepository).saveAll(anyList());
        verify(threaddRepository, never()).deleteByThreadId(any());
        verify(channel).basicAck(anyLong(), anyBoolean());
    }

    // 测试帖子不存在时删除索引文档
    @Test
    void shouldDeleteWhenThreadMissing() throws Exception {
        EsIndexSyncListener listener = createListener();
        when(threaddService.getById(1)).thenReturn(null);

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.THREAD, 1), createMessage(), channel);

        verify(threaddRepository).deleteByThreadId(1);
        verify(threaddRepository, never()).saveAll(anyList());
        verify(channel).basicAck(anyLong(), anyBoolean());
    }

    // 测试帖子已逻辑删除时删除索引文档
    @Test
    void shouldDeleteWhenThreadIsDeleted() throws Exception {
        EsIndexSyncListener listener = createListener();
        Threadd thread = new Threadd();
        thread.setThreadId(1);
        thread.setIsDeleted(true);
        when(threaddService.getById(1)).thenReturn(thread);

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.THREAD, 1), createMessage(), channel);

        verify(threaddRepository).deleteByThreadId(1);
        verify(threaddRepository, never()).saveAll(anyList());
    }

    // 测试有效回复消息触发文档 upsert
    @Test
    void shouldUpsertWhenPostAndThreadAreAlive() throws Exception {
        EsIndexSyncListener listener = createListener();
        Post post = new Post();
        post.setPostId(2);
        post.setThreadId(1);
        post.setIsDeleted(false);
        Threadd thread = new Threadd();
        thread.setThreadId(1);
        thread.setIsDeleted(false);
        when(postService.getById(2)).thenReturn(post);
        when(threaddService.getById(1)).thenReturn(thread);
        when(postService.toThreadDoc(List.of(post))).thenReturn(List.of(new ThreadDoc()));

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.POST, 2), createMessage(), channel);

        verify(threaddRepository).saveAll(anyList());
        verify(threaddRepository, never()).deleteById(any(String.class));
    }

    // 测试回复不存在时删除索引文档
    @Test
    void shouldDeleteWhenPostMissing() throws Exception {
        EsIndexSyncListener listener = createListener();
        when(postService.getById(2)).thenReturn(null);

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.POST, 2), createMessage(), channel);

        verify(threaddRepository).deleteById("POST-2");
        verify(threaddRepository, never()).saveAll(anyList());
    }

    // 测试回复已逻辑删除时删除索引文档
    @Test
    void shouldDeleteWhenPostIsDeleted() throws Exception {
        EsIndexSyncListener listener = createListener();
        Post post = new Post();
        post.setPostId(2);
        post.setThreadId(1);
        post.setIsDeleted(true);
        when(postService.getById(2)).thenReturn(post);

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.POST, 2), createMessage(), channel);

        verify(threaddRepository).deleteById("POST-2");
        verify(threaddRepository, never()).saveAll(anyList());
    }

    // 测试回复所属帖子已删除时不回写索引
    @Test
    void shouldDeleteWhenPostThreadIsDeleted() throws Exception {
        EsIndexSyncListener listener = createListener();
        Post post = new Post();
        post.setPostId(2);
        post.setThreadId(1);
        post.setIsDeleted(false);
        Threadd thread = new Threadd();
        thread.setThreadId(1);
        thread.setIsDeleted(true);
        when(postService.getById(2)).thenReturn(post);
        when(threaddService.getById(1)).thenReturn(thread);

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.POST, 2), createMessage(), channel);

        verify(threaddRepository).deleteById("POST-2");
        verify(threaddRepository, never()).saveAll(anyList());
    }

    // 测试全量重建命令触发索引重建
    @Test
    void shouldRebuildAllWhenReceivingAllMessage() throws Exception {
        EsIndexSyncListener listener = createListener();

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.ALL, null), createMessage(), channel);

        verify(esIndexService).initIndex();
        verify(channel).basicAck(anyLong(), anyBoolean());
    }

    // 测试空实体 ID 的帖子消息被忽略
    @Test
    void shouldIgnoreWhenEntityIdIsNull() throws Exception {
        EsIndexSyncListener listener = createListener();

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.THREAD, null), createMessage(), channel);

        verifyNoInteractions(threaddRepository);
        verify(channel).basicAck(anyLong(), anyBoolean());
    }

    // 测试消费异常时拒绝消息且不入队重试
    @Test
    void shouldNackWithoutRequeueWhenSyncFails() throws Exception {
        EsIndexSyncListener listener = createListener();
        doThrow(new RuntimeException("es down")).when(threaddRepository).deleteByThreadId(1);
        when(threaddService.getById(1)).thenReturn(null);

        listener.onMessage(new EsIndexSyncMessage(EsIndexEntityType.THREAD, 1), createMessage(), channel);

        verify(channel).basicNack(anyLong(), anyBoolean(), org.mockito.ArgumentMatchers.eq(false));
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
