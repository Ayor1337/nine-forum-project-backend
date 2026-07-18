package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.FeedbackCreateDTO;
import com.ayor.entity.pojo.Feedback;
import com.ayor.entity.vo.FeedbackVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户反馈服务接口
 *
 * 处理用户提交的反馈内容，支持反馈创建和历史查询。
 *
 * 主要功能:
 * - 创建反馈
 * - 查看我的反馈列表
 *
 * @see Feedback 反馈实体
 * @see FeedbackVO 反馈视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface FeedbackService extends IService<Feedback> {

    String createFeedback(Integer accountId, FeedbackCreateDTO dto);

    PageEntity<FeedbackVO> getMyFeedbacks(Integer accountId, Integer pageNum, Integer pageSize);
}
