package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackCreateDTO;
import com.ayor.entity.pojo.Feedback;
import com.ayor.entity.vo.FeedbackVO;
import com.ayor.mapper.FeedbackMapper;
import com.ayor.service.FeedbackService;
import com.ayor.type.FeedbackStatus;
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

/**
 * 反馈服务实现
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final FeedbackMapper feedbackMapper;

    @Override
    public String createFeedback(Integer accountId, FeedbackCreateDTO dto) {
        if (accountId == null || accountId <= 0) {
            return "用户不存在";
        }
        if (dto == null || dto.getType() == null) {
            return "反馈类型不能为空";
        }
        if (!StringUtils.hasText(dto.getContent())) {
            return "反馈内容不能为空";
        }
        String content = dto.getContent().trim();
        if (content.length() < 10 || content.length() > 1000) {
            return "反馈内容长度必须在10到1000个字符之间";
        }

        Date now = new Date();
        Feedback feedback = new Feedback();
        feedback.setAccountId(accountId);
        feedback.setType(dto.getType());
        feedback.setContent(content);
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setCreateTime(now);
        feedback.setUpdateTime(now);
        return feedbackMapper.insert(feedback) > 0 ? null : "提交反馈失败";
    }

    @Override
    public PageEntity<FeedbackVO> getMyFeedbacks(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null || accountId <= 0 || !isValidPage(pageNum, pageSize)) {
            return null;
        }
        LambdaQueryWrapper<Feedback> query = new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getAccountId, accountId)
                .orderByDesc(Feedback::getCreateTime);
        Page<Feedback> page = feedbackMapper.selectPage(Page.of(pageNum, pageSize), query);
        List<FeedbackVO> data = page.getRecords().stream().map(this::toVO).toList();
        return new PageEntity<>(page.getTotal(), data);
    }

    private boolean isValidPage(Integer pageNum, Integer pageSize) {
        return pageNum != null && pageNum >= 1
                && pageSize != null && pageSize >= 1 && pageSize <= 100;
    }

    private FeedbackVO toVO(Feedback feedback) {
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        return vo;
    }
}
