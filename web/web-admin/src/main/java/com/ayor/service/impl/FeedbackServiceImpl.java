package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackHandleDTO;
import com.ayor.entity.pojo.Feedback;
import com.ayor.entity.vo.FeedbackVO;
import com.ayor.mapper.FeedbackMapper;
import com.ayor.service.FeedbackService;
import com.ayor.type.FeedbackStatus;
import com.ayor.type.FeedbackType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public PageEntity<FeedbackVO> getFeedbacks(Integer pageNum,
                                               Integer pageSize,
                                               FeedbackStatus status,
                                               FeedbackType type,
                                               Integer accountId) {
        if (!isValidPage(pageNum, pageSize)) {
            return null;
        }
        LambdaQueryWrapper<Feedback> query = new LambdaQueryWrapper<Feedback>()
                .eq(status != null, Feedback::getStatus, status)
                .eq(type != null, Feedback::getType, type)
                .eq(accountId != null, Feedback::getAccountId, accountId)
                .orderByDesc(Feedback::getCreateTime);
        Page<Feedback> page = feedbackMapper.selectPage(Page.of(pageNum, pageSize), query);
        List<FeedbackVO> data = page.getRecords().stream().map(this::toVO).toList();
        return new PageEntity<>(page.getTotal(), data);
    }

    @Override
    public FeedbackVO getFeedbackDetail(Integer feedbackId) {
        if (feedbackId == null || feedbackId <= 0) {
            return null;
        }
        return toVO(feedbackMapper.selectById(feedbackId));
    }

    @Override
    public String handleFeedback(Integer feedbackId, Integer handlerAccountId, FeedbackHandleDTO dto) {
        if (feedbackId == null || feedbackId <= 0) {
            return "反馈不存在";
        }
        if (handlerAccountId == null || handlerAccountId <= 0) {
            return "管理员不存在";
        }
        if (dto == null || dto.getStatus() == null) {
            return "处理状态不能为空";
        }
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            return "反馈不存在";
        }
        if (isFinalStatus(feedback.getStatus())) {
            return "反馈已处理完成";
        }
        if (!canTransition(feedback.getStatus(), dto.getStatus())) {
            return "不支持的状态流转";
        }

        String handleNote = StringUtils.hasText(dto.getHandleNote()) ? dto.getHandleNote().trim() : null;
        if (handleNote != null && handleNote.length() > 500) {
            return "处理备注不能超过500个字符";
        }
        if (isFinalStatus(dto.getStatus()) && !StringUtils.hasText(handleNote)) {
            return "处理备注不能为空";
        }

        Date now = new Date();
        feedback.setStatus(dto.getStatus());
        feedback.setHandlerAccountId(handlerAccountId);
        feedback.setHandleNote(handleNote);
        feedback.setUpdateTime(now);
        if (isFinalStatus(dto.getStatus())) {
            feedback.setHandledAt(now);
        }
        return feedbackMapper.updateById(feedback) > 0 ? null : "反馈处理失败";
    }

    private boolean canTransition(FeedbackStatus current, FeedbackStatus target) {
        if (current == FeedbackStatus.PENDING) {
            return target == FeedbackStatus.PROCESSING
                    || target == FeedbackStatus.RESOLVED
                    || target == FeedbackStatus.CLOSED;
        }
        if (current == FeedbackStatus.PROCESSING) {
            return target == FeedbackStatus.RESOLVED || target == FeedbackStatus.CLOSED;
        }
        return false;
    }

    private boolean isFinalStatus(FeedbackStatus status) {
        return status == FeedbackStatus.RESOLVED || status == FeedbackStatus.CLOSED;
    }

    private boolean isValidPage(Integer pageNum, Integer pageSize) {
        return pageNum != null && pageNum >= 1
                && pageSize != null && pageSize >= 1 && pageSize <= 100;
    }

    private FeedbackVO toVO(Feedback feedback) {
        if (feedback == null) {
            return null;
        }
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        return vo;
    }
}
