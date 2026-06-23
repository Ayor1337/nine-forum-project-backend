package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackCreateDTO;
import com.ayor.entity.pojo.Feedback;
import com.ayor.entity.vo.FeedbackVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FeedbackService extends IService<Feedback> {

    String createFeedback(Integer accountId, FeedbackCreateDTO dto);

    PageEntity<FeedbackVO> getMyFeedbacks(Integer accountId, Integer pageNum, Integer pageSize);
}
