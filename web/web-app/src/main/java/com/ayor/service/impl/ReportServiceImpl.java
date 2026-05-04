package com.ayor.service.impl;

import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.UserReportDTO;
import com.ayor.entity.message.ReportCreatedMessage;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Post;
import com.ayor.entity.pojo.Report;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PostMapper;
import com.ayor.mapper.ReportMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.ReportService;
import com.ayor.type.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    private final AccountMapper accountMapper;

    private final ThreaddMapper threaddMapper;

    private final PostMapper postMapper;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public String createUserReport(Integer reporterAccountId, Integer reportedAccountId, UserReportDTO dto) {
        if (reporterAccountId == null || reporterAccountId == 0) {
            return "用户不存在";
        }
        if (reportedAccountId == null) {
            return "被举报用户不存在";
        }
        if (reporterAccountId.equals(reportedAccountId)) {
            return "不能举报自己";
        }
        String descriptionError = validateDescription(dto == null ? null : dto.getDescription());
        if (descriptionError != null) {
            return descriptionError;
        }
        if (dto == null || dto.getType() == null) {
            return "举报类型不能为空";
        }
        if (findActiveReport(reporterAccountId, ReportTargetType.USER, reportedAccountId) != null) {
            return "请勿重复举报，当前举报正在处理中";
        }
        Account reportedAccount = accountMapper.getAccountById(reportedAccountId);
        if (reportedAccount == null) {
            return "被举报用户不存在";
        }
        sendReportCreatedMessage(ReportCreatedMessage.builder()
                .reporterAccountId(reporterAccountId)
                .reportedAccountId(reportedAccountId)
                .targetType(ReportTargetType.USER)
                .targetId(reportedAccountId)
                .reportType(dto.getType().name())
                .description(dto.getDescription().trim())
                .reportedUsernameSnapshot(resolveReportedUsername(reportedAccount))
                .targetSummarySnapshot(resolveReportedUsername(reportedAccount))
                .build());
        return null;
    }

    @Override
    public String createThreadReport(Integer reporterAccountId, Integer threadId, ContentReportDTO dto) {
        if (reporterAccountId == null || reporterAccountId == 0) {
            return "用户不存在";
        }
        if (threadId == null) {
            return "帖子不存在";
        }
        String descriptionError = validateDescription(dto == null ? null : dto.getDescription());
        if (descriptionError != null) {
            return descriptionError;
        }
        if (dto == null || dto.getType() == null) {
            return "举报类型不能为空";
        }
        if (findActiveReport(reporterAccountId, ReportTargetType.THREAD, threadId) != null) {
            return "请勿重复举报，当前举报正在处理中";
        }
        Threadd thread = threaddMapper.selectById(threadId);
        if (thread == null || Boolean.TRUE.equals(thread.getIsDeleted())) {
            return "帖子不存在";
        }
        if (reporterAccountId.equals(thread.getAccountId())) {
            return "不能举报自己";
        }
        Account reportedAccount = accountMapper.getAccountById(thread.getAccountId());
        if (reportedAccount == null) {
            return "被举报用户不存在";
        }
        sendReportCreatedMessage(ReportCreatedMessage.builder()
                .reporterAccountId(reporterAccountId)
                .reportedAccountId(thread.getAccountId())
                .targetType(ReportTargetType.THREAD)
                .targetId(threadId)
                .reportType(dto.getType().name())
                .description(dto.getDescription().trim())
                .reportedUsernameSnapshot(resolveReportedUsername(reportedAccount))
                .targetSummarySnapshot(resolveThreadSummary(thread))
                .build());
        return null;
    }

    @Override
    public String createPostReport(Integer reporterAccountId, Integer postId, ContentReportDTO dto) {
        if (reporterAccountId == null || reporterAccountId == 0) {
            return "用户不存在";
        }
        if (postId == null) {
            return "评论不存在";
        }
        String descriptionError = validateDescription(dto == null ? null : dto.getDescription());
        if (descriptionError != null) {
            return descriptionError;
        }
        if (dto == null || dto.getType() == null) {
            return "举报类型不能为空";
        }
        if (findActiveReport(reporterAccountId, ReportTargetType.POST, postId) != null) {
            return "请勿重复举报，当前举报正在处理中";
        }
        Post post = postMapper.selectById(postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return "评论不存在";
        }
        if (reporterAccountId.equals(post.getAccountId())) {
            return "不能举报自己";
        }
        Account reportedAccount = accountMapper.getAccountById(post.getAccountId());
        if (reportedAccount == null) {
            return "被举报用户不存在";
        }
        sendReportCreatedMessage(ReportCreatedMessage.builder()
                .reporterAccountId(reporterAccountId)
                .reportedAccountId(post.getAccountId())
                .targetType(ReportTargetType.POST)
                .targetId(postId)
                .reportType(dto.getType().name())
                .description(dto.getDescription().trim())
                .reportedUsernameSnapshot(resolveReportedUsername(reportedAccount))
                .targetSummarySnapshot(truncate(post.getContent()))
                .build());
        return null;
    }

    public Report findActiveReport(Integer reporterAccountId, ReportTargetType targetType, Integer targetId) {
        return reportMapper.selectActiveReport(reporterAccountId, targetType.name(), targetId);
    }

    public void sendReportCreatedMessage(ReportCreatedMessage message) {
        rabbitTemplate.convertAndSend("report.direct", "report.created", message);
    }

    private String validateDescription(String description) {
        if (!StringUtils.hasText(description)) {
            return "举报描述不能为空";
        }
        String normalized = description.trim();
        if (normalized.length() < 10 || normalized.length() > 500) {
            return "举报描述长度应在 10 到 500 个字符之间";
        }
        return null;
    }

    private String resolveReportedUsername(Account account) {
        if (account == null) {
            return null;
        }
        if (StringUtils.hasText(account.getNickname())) {
            return account.getNickname();
        }
        return account.getUsername();
    }

    private String resolveThreadSummary(Threadd thread) {
        if (thread == null) {
            return null;
        }
        if (StringUtils.hasText(thread.getTitle())) {
            return truncate(thread.getTitle());
        }
        return truncate(thread.getContent());
    }

    private String truncate(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String normalized = text.trim();
        return normalized.length() <= 60 ? normalized : normalized.substring(0, 60);
    }
}
