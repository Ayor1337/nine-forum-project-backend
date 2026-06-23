package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackHandleDTO;
import com.ayor.entity.pojo.Feedback;
import com.ayor.entity.vo.FeedbackVO;
import com.ayor.type.FeedbackStatus;
import com.ayor.type.FeedbackType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FeedbackService extends IService<Feedback> {

    PageEntity<FeedbackVO> getFeedbacks(Integer pageNum,
                                        Integer pageSize,
                                        FeedbackStatus status,
                                        FeedbackType type,
                                        Integer accountId);

    FeedbackVO getFeedbackDetail(Integer feedbackId);

    String handleFeedback(Integer feedbackId, Integer handlerAccountId, FeedbackHandleDTO dto);
}
