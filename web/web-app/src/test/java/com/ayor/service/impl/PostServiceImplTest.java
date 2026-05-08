package com.ayor.service.impl;

import com.ayor.entity.dto.PostDTO;
import com.ayor.entity.pojo.Post;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ThreaddMapper;
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

import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void shouldSyncImageRefsAfterSavingPost() {
        TipTapUtils tipTapUtils = new TipTapUtils();
        PostServiceImpl service = new PostServiceImpl(
                postMapper,
                accountMapper,
                tipTapUtils,
                threaddMapper,
                messagingTemplate,
                stompUtils,
                mentionMessageService,
                imageAssetService
        );
        ReflectionTestUtils.setField(service, "baseMapper", postMapper);

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
}
