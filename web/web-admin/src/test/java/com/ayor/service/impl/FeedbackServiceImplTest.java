package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackHandleDTO;
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

    // 测试待处理反馈可以流转到处理中
    @Test
    void pendingFeedbackCanMoveToProcessing() {
        Feedback feedback = feedback(1, FeedbackStatus.PENDING);
        when(feedbackMapper.selectById(1)).thenReturn(feedback);
        when(feedbackMapper.updateById(any(Feedback.class))).thenReturn(1);
        FeedbackHandleDTO dto = handleDto(FeedbackStatus.PROCESSING, "  已安排处理  ");

        String result = feedbackService.handleFeedback(1, 9, dto);

        ArgumentCaptor<Feedback> captor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackMapper).updateById(captor.capture());
        Feedback updated = captor.getValue();
        assertThat(result).isNull();
        assertThat(updated.getStatus()).isEqualTo(FeedbackStatus.PROCESSING);
        assertThat(updated.getHandlerAccountId()).isEqualTo(9);
        assertThat(updated.getHandleNote()).isEqualTo("已安排处理");
        assertThat(updated.getHandledAt()).isNull();
        assertThat(updated.getUpdateTime()).isNotNull();
    }

    // 测试终态状态要求处理备注
    @Test
    void finalStatusRequiresHandleNote() {
        when(feedbackMapper.selectById(1)).thenReturn(feedback(1, FeedbackStatus.PROCESSING));

        String result = feedbackService.handleFeedback(
                1, 9, handleDto(FeedbackStatus.RESOLVED, "   "));

        assertThat(result).isEqualTo("处理备注不能为空");
        verify(feedbackMapper, never()).updateById(any(Feedback.class));
    }

    // 测试处理中反馈可设为已解决并记录处理时间
    @Test
    void processingFeedbackCanBeResolvedAndRecordsHandledTime() {
        Feedback feedback = feedback(1, FeedbackStatus.PROCESSING);
        when(feedbackMapper.selectById(1)).thenReturn(feedback);
        when(feedbackMapper.updateById(any(Feedback.class))).thenReturn(1);

        String result = feedbackService.handleFeedback(
                1, 9, handleDto(FeedbackStatus.RESOLVED, "问题已修复"));

        assertThat(result).isNull();
        assertThat(feedback.getStatus()).isEqualTo(FeedbackStatus.RESOLVED);
        assertThat(feedback.getHandledAt()).isNotNull();
    }

    // 测试终态反馈不能再次变更
    @Test
    void finalFeedbackCannotBeChangedAgain() {
        when(feedbackMapper.selectById(1)).thenReturn(feedback(1, FeedbackStatus.CLOSED));

        String result = feedbackService.handleFeedback(
                1, 9, handleDto(FeedbackStatus.RESOLVED, "重新处理"));

        assertThat(result).isEqualTo("反馈已处理完成");
        verify(feedbackMapper, never()).updateById(any(Feedback.class));
    }

    // 测试拒绝不支持状态流转
    @Test
    void rejectsUnsupportedStatusTransition() {
        when(feedbackMapper.selectById(1)).thenReturn(feedback(1, FeedbackStatus.PROCESSING));

        String result = feedbackService.handleFeedback(
                1, 9, handleDto(FeedbackStatus.PENDING, null));

        assertThat(result).isEqualTo("不支持的状态流转");
        verify(feedbackMapper, never()).updateById(any(Feedback.class));
    }

    // 测试缺失反馈不能被处理
    @Test
    void missingFeedbackCannotBeHandled() {
        when(feedbackMapper.selectById(404)).thenReturn(null);

        String result = feedbackService.handleFeedback(
                404, 9, handleDto(FeedbackStatus.CLOSED, "无效反馈"));

        assertThat(result).isEqualTo("反馈不存在");
    }

    // 测试获取反馈返回分页后台视图
    @Test
    @SuppressWarnings("unchecked")
    void getFeedbacksReturnsPagedAdminView() {
        Feedback feedback = feedback(1, FeedbackStatus.PENDING);
        when(feedbackMapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(invocation -> {
            Page<Feedback> page = invocation.getArgument(0);
            page.setRecords(List.of(feedback));
            page.setTotal(1);
            return page;
        });

        PageEntity<FeedbackVO> result = feedbackService.getFeedbacks(
                1, 10, FeedbackStatus.PENDING, FeedbackType.PROBLEM, 7);

        assertThat(result.getTotalSize()).isEqualTo(1);
        assertThat(result.getData()).singleElement().satisfies(vo -> {
            assertThat(vo.getFeedbackId()).isEqualTo(1);
            assertThat(vo.getAccountId()).isEqualTo(7);
        });
    }

    // 测试获取反馈拒绝无效分页
    @Test
    void getFeedbacksRejectsInvalidPagination() {
        assertThat(feedbackService.getFeedbacks(1, 0, null, null, null)).isNull();
        assertThat(feedbackService.getFeedbacks(1, 101, null, null, null)).isNull();
        verify(feedbackMapper, never()).selectPage(any(Page.class), any(Wrapper.class));
    }

    private Feedback feedback(Integer feedbackId, FeedbackStatus status) {
        Feedback feedback = new Feedback();
        feedback.setFeedbackId(feedbackId);
        feedback.setAccountId(7);
        feedback.setType(FeedbackType.PROBLEM);
        feedback.setContent("页面在特定情况下无法正常加载");
        feedback.setStatus(status);
        feedback.setCreateTime(new Date());
        feedback.setUpdateTime(new Date());
        return feedback;
    }

    private FeedbackHandleDTO handleDto(FeedbackStatus status, String handleNote) {
        FeedbackHandleDTO dto = new FeedbackHandleDTO();
        dto.setStatus(status);
        dto.setHandleNote(handleNote);
        return dto;
    }
}
