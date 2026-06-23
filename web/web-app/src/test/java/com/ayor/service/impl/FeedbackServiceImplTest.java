package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackCreateDTO;
import com.ayor.entity.pojo.Feedback;
import com.ayor.entity.vo.FeedbackVO;
import com.ayor.mapper.FeedbackMapper;
import com.ayor.type.FeedbackStatus;
import com.ayor.type.FeedbackType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackMapper feedbackMapper;

    private FeedbackServiceImpl feedbackService;

    @BeforeEach
    void setUp() {
        feedbackService = new FeedbackServiceImpl(feedbackMapper);
        ReflectionTestUtils.setField(feedbackService, "baseMapper", feedbackMapper);
    }

    @Test
    void createFeedbackBindsUserAndInitializesPendingStatus() {
        FeedbackCreateDTO dto = new FeedbackCreateDTO();
        dto.setType(FeedbackType.SUGGESTION);
        dto.setContent("  希望增加夜间模式切换功能  ");
        when(feedbackMapper.insert(any(Feedback.class))).thenReturn(1);

        String result = feedbackService.createFeedback(7, dto);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).insert(captor.capture());
        Feedback feedback = captor.getValue();
        assertThat(result).isNull();
        assertThat(feedback.getAccountId()).isEqualTo(7);
        assertThat(feedback.getContent()).isEqualTo("希望增加夜间模式切换功能");
        assertThat(feedback.getStatus()).isEqualTo(FeedbackStatus.PENDING);
        assertThat(feedback.getCreateTime()).isNotNull();
        assertThat(feedback.getUpdateTime()).isNotNull();
    }

    @Test
    void createFeedbackRejectsContentShorterThanTenAfterTrim() {
        FeedbackCreateDTO dto = new FeedbackCreateDTO();
        dto.setType(FeedbackType.OTHER);
        dto.setContent("   内容太短   ");

        String result = feedbackService.createFeedback(7, dto);

        assertThat(result).isEqualTo("反馈内容长度必须在10到1000个字符之间");
        verify(feedbackMapper, never()).insert(any(Feedback.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getMyFeedbacksReturnsOnlyPagedRecordsAsUserView() {
        Feedback feedback = feedback(3, 7, FeedbackStatus.PROCESSING);
        when(feedbackMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<Feedback> page = invocation.getArgument(0);
            page.setRecords(List.of(feedback));
            page.setTotal(1);
            return page;
        });

        PageEntity<FeedbackVO> result = feedbackService.getMyFeedbacks(7, 1, 10);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getData()).singleElement().satisfies(vo -> {
            assertThat(vo.getFeedbackId()).isEqualTo(3);
            assertThat(vo.getStatus()).isEqualTo(FeedbackStatus.PROCESSING);
            assertThat(vo.getClass().getDeclaredFields())
                    .noneMatch(field -> field.getName().equals("handlerAccountId")
                            || field.getName().equals("accountId"));
        });
        verify(feedbackMapper).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    void getMyFeedbacksRejectsInvalidPagination() {
        assertThat(feedbackService.getMyFeedbacks(7, 0, 10)).isNull();
        assertThat(feedbackService.getMyFeedbacks(7, 1, 101)).isNull();
        verify(feedbackMapper, never()).selectPage(any(Page.class), any(Wrapper.class));
    }

    private Feedback feedback(Integer feedbackId, Integer accountId, FeedbackStatus status) {
        Feedback feedback = new Feedback();
        feedback.setFeedbackId(feedbackId);
        feedback.setAccountId(accountId);
        feedback.setType(FeedbackType.PROBLEM);
        feedback.setContent("页面在特定情况下无法正常加载");
        feedback.setStatus(status);
        feedback.setCreateTime(new Date());
        feedback.setUpdateTime(new Date());
        return feedback;
    }
}
