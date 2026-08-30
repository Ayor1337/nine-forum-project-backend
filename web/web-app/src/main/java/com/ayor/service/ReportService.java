package com.ayor.service;

import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.UserReportDTO;

/**
 * 举报服务接口
 *
 * 处理用户举报功能，支持举报用户、帖子和评论。
 *
 * 主要功能:
 * - 举报用户
 * - 举报帖子
 * - 举报评论
 *
 * 技术特性:
 * - 举报后通过 RabbitMQ 异步通知管理员
 *
 * @author ayor
 * @since 1.0.0
 */
public interface ReportService {

    String createUserReport(Integer reporterAccountId, Integer reportedAccountId, UserReportDTO dto);

    String createThreadReport(Integer reporterAccountId, Integer threadId, ContentReportDTO dto);

    String createPostReport(Integer reporterAccountId, Integer postId, ContentReportDTO dto);
}
