package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ReportHandleDTO;
import com.ayor.entity.message.ReportCreatedMessage;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.pojo.Report;
import com.ayor.entity.stomp.ReportStompMessage;
import com.ayor.entity.vo.ReportVO;
import com.ayor.mapper.ReportMapper;
import com.ayor.service.ReportService;
import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;

    private final RabbitTemplate rabbitTemplate;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void createFromMessage(ReportCreatedMessage message) {
        Report report = new Report();
        report.setReporterAccountId(message.getReporterAccountId());
        report.setReportedAccountId(message.getReportedAccountId());
        report.setTargetType(message.getTargetType());
        report.setTargetId(message.getTargetId());
        report.setReportType(message.getReportType());
        report.setDescription(message.getDescription());
        report.setStatus(ReportStatus.PENDING);
        report.setReportedUsernameSnapshot(message.getReportedUsernameSnapshot());
        report.setTargetSummarySnapshot(message.getTargetSummarySnapshot());
        Date now = new Date();
        report.setCreateTime(now);
        report.setUpdateTime(now);
        reportMapper.insert(report);
        messagingTemplate.convertAndSend("/topic/reports", toStompMessage(report));
    }

    @Override
    public PageEntity<ReportVO> getReports(Integer pageNum,
                                           Integer pageSize,
                                           ReportStatus status,
                                           ReportTargetType targetType,
                                           String reportType,
                                           Integer reporterAccountId,
                                           Integer reportedAccountId) {
        Page<Report> page = this.lambdaQuery()
                .eq(status != null, Report::getStatus, status)
                .eq(targetType != null, Report::getTargetType, targetType)
                .eq(StringUtils.hasText(reportType), Report::getReportType, reportType)
                .eq(reporterAccountId != null, Report::getReporterAccountId, reporterAccountId)
                .eq(reportedAccountId != null, Report::getReportedAccountId, reportedAccountId)
                .orderByDesc(Report::getCreateTime)
                .page(Page.of(pageNum == null || pageNum < 1 ? 1 : pageNum, pageSize == null || pageSize < 1 ? 10 : pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public ReportVO getReportDetail(Integer reportId) {
        if (reportId == null) {
            return null;
        }
        return toVO(this.getById(reportId));
    }

    @Override
    public String handleReport(Integer reportId, Integer handlerAccountId, ReportHandleDTO dto) {
        if (reportId == null) {
            return "举报不存在";
        }
        if (handlerAccountId == null || handlerAccountId == 0) {
            return "管理员不存在";
        }
        if (dto == null || dto.getStatus() == null) {
            return "处理状态不能为空";
        }
        Report report = this.getById(reportId);
        if (report == null) {
            return "举报不存在";
        }
        if (isFinalStatus(report.getStatus())) {
            return "举报已处理完成";
        }
        if (dto.getStatus() == ReportStatus.PENDING) {
            return "不支持回退到待处理";
        }
        if (report.getHandlerAccountId() != null && !report.getHandlerAccountId().equals(handlerAccountId)) {
            return "该举报已由其他管理员接手";
        }
        if ((dto.getStatus() == ReportStatus.RESOLVED || dto.getStatus() == ReportStatus.REJECTED)
                && !StringUtils.hasText(dto.getHandleNote())) {
            return "处理备注不能为空";
        }

        report.setHandlerAccountId(handlerAccountId);
        report.setStatus(dto.getStatus());
        report.setUpdateTime(new Date());
        if (StringUtils.hasText(dto.getHandleNote())) {
            report.setHandleNote(dto.getHandleNote().trim());
        }
        if (dto.getStatus() == ReportStatus.RESOLVED || dto.getStatus() == ReportStatus.REJECTED) {
            report.setHandledAt(new Date());
        }
        if (!this.updateById(report)) {
            return "举报处理失败";
        }
        if (dto.getStatus() == ReportStatus.RESOLVED || dto.getStatus() == ReportStatus.REJECTED) {
            notifyReporter(report);
        }
        return null;
    }

    private void notifyReporter(Report report) {
        String action = report.getStatus() == ReportStatus.RESOLVED ? "已处理" : "已驳回";
        String content = "您提交的举报" + action + "：" + safe(report.getTargetSummarySnapshot()) + "。";
        sendReportResultMessage(new UserSystemMessage<>(content, "举报处理结果", report.getReporterAccountId()));
    }

    public void sendReportResultMessage(UserSystemMessage<String> message) {
        rabbitTemplate.convertAndSend("broadcast.direct", "broadcast", message);
    }

    private boolean isFinalStatus(ReportStatus status) {
        return status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED;
    }

    private List<ReportVO> toVOList(List<Report> reports) {
        List<ReportVO> vos = new ArrayList<>();
        for (Report report : reports) {
            vos.add(toVO(report));
        }
        return vos;
    }

    private ReportVO toVO(Report report) {
        if (report == null) {
            return null;
        }
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    private ReportStompMessage toStompMessage(Report report) {
        return ReportStompMessage.builder()
                .reportId(report.getReportId())
                .reporterAccountId(report.getReporterAccountId())
                .reportedAccountId(report.getReportedAccountId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reportType(report.getReportType())
                .reportedUsernameSnapshot(report.getReportedUsernameSnapshot())
                .targetSummarySnapshot(report.getTargetSummarySnapshot())
                .status(report.getStatus())
                .createTime(report.getCreateTime())
                .build();
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "相关内容";
    }
}
