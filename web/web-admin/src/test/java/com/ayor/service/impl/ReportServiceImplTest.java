package com.ayor.service.impl;

import com.ayor.entity.dto.ReportHandleDTO;
import com.ayor.entity.message.ReportCreatedMessage;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.pojo.Report;
import com.ayor.entity.stomp.ReportStompMessage;
import com.ayor.mapper.ReportMapper;
import com.ayor.service.AccountService;
import com.ayor.type.AccountAction;
import com.ayor.type.AccountStatus;
import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AccountService accountService;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(reportMapper, rabbitTemplate, messagingTemplate, accountService);
        ReflectionTestUtils.setField(reportService, "baseMapper", reportMapper);
    }

    // 测试从消息创建举报时持久化待处理记录并推送STOMP消息
    @Test
    void createFromMessagePersistsPendingReportAndPushesStompMessage() {
        ReportCreatedMessage message = ReportCreatedMessage.builder()
                .reporterAccountId(1)
                .reportedAccountId(2)
                .targetType(ReportTargetType.POST)
                .targetId(9)
                .reportType("SPAM")
                .description("举报描述")
                .reportedUsernameSnapshot("reported")
                .targetSummarySnapshot("summary")
                .build();

        reportService.createFromMessage(message);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        ArgumentCaptor<ReportStompMessage> stompCaptor = ArgumentCaptor.forClass(ReportStompMessage.class);
        verify(reportMapper).insert(reportCaptor.capture());
        verify(messagingTemplate).convertAndSend(eq("/topic/reports"), stompCaptor.capture());
        assertThat(reportCaptor.getValue().getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(reportCaptor.getValue().getTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(reportCaptor.getValue().getCreateTime()).isNotNull();
        assertThat(stompCaptor.getValue().getTargetSummarySnapshot()).isEqualTo("summary");
    }

    // 测试处理举报会拒绝无效输入和终态记录
    @Test
    void handleReportRejectsInvalidInputsAndFinalStates() {
        assertThat(reportService.handleReport(null, 7, dto(ReportStatus.PROCESSING, null, null)))
                .isEqualTo("举报不存在");
        assertThat(reportService.handleReport(1, null, dto(ReportStatus.PROCESSING, null, null)))
                .isEqualTo("管理员不存在");
        assertThat(reportService.handleReport(1, 7, dto(null, null, null)))
                .isEqualTo("处理状态不能为空");

        when(reportMapper.selectById(1)).thenReturn(null);
        assertThat(reportService.handleReport(1, 7, dto(ReportStatus.PROCESSING, null, null)))
                .isEqualTo("举报不存在");

        when(reportMapper.selectById(2)).thenReturn(report(2, ReportStatus.RESOLVED, null));
        assertThat(reportService.handleReport(2, 7, dto(ReportStatus.REJECTED, "done", null)))
                .isEqualTo("举报已处理完成");
    }

    // 测试处理举报拒绝不支持的流转并归属冲突
    @Test
    void handleReportRejectsUnsupportedTransitionsAndOwnershipConflict() {
        when(reportMapper.selectById(1)).thenReturn(report(1, ReportStatus.PROCESSING, null));
        assertThat(reportService.handleReport(1, 7, dto(ReportStatus.PENDING, null, null)))
                .isEqualTo("不支持回退到待处理");

        when(reportMapper.selectById(2)).thenReturn(report(2, ReportStatus.PROCESSING, 99));
        assertThat(reportService.handleReport(2, 7, dto(ReportStatus.RESOLVED, "已处理", null)))
                .isEqualTo("该举报已由其他管理员接手");
    }

    // 测试处理举报要求备注用于终态状态并拒绝无效处罚
    @Test
    void handleReportRequiresNoteForFinalStatusAndRejectsInvalidPunishment() {
        when(reportMapper.selectById(1)).thenReturn(report(1, ReportStatus.PROCESSING, null));
        assertThat(reportService.handleReport(1, 7, dto(ReportStatus.RESOLVED, " ", null)))
                .isEqualTo("处理备注不能为空");

        when(reportMapper.selectById(2)).thenReturn(report(2, ReportStatus.PROCESSING, null));
        assertThat(reportService.handleReport(2, 7, dto(ReportStatus.REJECTED, "驳回", AccountAction.MUTE)))
                .isEqualTo("驳回举报时不能执行账号处罚");

        when(reportMapper.selectById(3)).thenReturn(report(3, ReportStatus.PROCESSING, null));
        assertThat(reportService.handleReport(3, 7, dto(ReportStatus.PROCESSING, null, AccountAction.MUTE)))
                .isEqualTo("当前处理状态不支持账号处罚");
    }

    // 测试处理举报为已解决时执行禁言并通知举报人
    @Test
    void handleReportResolvesAppliesMuteAndNotifiesReporter() {
        Report report = report(1, ReportStatus.PROCESSING, null);
        report.setReportedAccountId(2);
        report.setReporterAccountId(1);
        report.setTargetSummarySnapshot("帖子摘要");
        when(reportMapper.selectById(1)).thenReturn(report);
        when(reportMapper.updateById(report)).thenReturn(1);
        when(accountService.updateAccountStatus(2, AccountStatus.MUTED, "违规成立")).thenReturn(null);

        String result = reportService.handleReport(1, 7, dto(ReportStatus.RESOLVED, "违规成立", AccountAction.MUTE));

        ArgumentCaptor<UserSystemMessage<String>> captor = ArgumentCaptor.forClass(UserSystemMessage.class);
        assertThat(result).isNull();
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getHandlerAccountId()).isEqualTo(7);
        assertThat(report.getHandledAt()).isNotNull();
        verify(accountService).updateAccountStatus(2, AccountStatus.MUTED, "违规成立");
        verify(rabbitTemplate).convertAndSend(eq("broadcast.direct"), eq("broadcast"), captor.capture());
        assertThat(captor.getValue().getSendTo()).isEqualTo(1);
        assertThat(captor.getValue().getMessage()).contains("已处理");
    }

    // 测试处理举报在更新失败时不会执行处罚和通知
    @Test
    void handleReportRejectsUpdateFailureBeforePunishmentAndNotify() {
        Report report = report(1, ReportStatus.PROCESSING, null);
        when(reportMapper.selectById(1)).thenReturn(report);
        when(reportMapper.updateById(report)).thenReturn(0);

        String result = reportService.handleReport(1, 7, dto(ReportStatus.RESOLVED, "违规成立", AccountAction.BAN));

        assertThat(result).isEqualTo("举报处理失败");
        verify(accountService, never()).updateAccountStatus(any(), any(), any());
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // 测试处理举报更新成功但账号处罚失败时返回错误
    @Test
    void handleReportReturnsAccountActionErrorAfterSuccessfulUpdate() {
        Report report = report(1, ReportStatus.PROCESSING, null);
        report.setReportedAccountId(2);
        when(reportMapper.selectById(1)).thenReturn(report);
        when(reportMapper.updateById(report)).thenReturn(1);
        when(accountService.updateAccountStatus(2, AccountStatus.BANNED, "违规成立")).thenReturn("账号更新失败");

        String result = reportService.handleReport(1, 7, dto(ReportStatus.RESOLVED, "违规成立", AccountAction.BAN));

        assertThat(result).isEqualTo("账号更新失败");
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    // 测试获取举报详情时映射VO并拒绝空ID
    @Test
    void getReportDetailMapsReportToVoAndRejectsNullId() {
        Report report = report(1, ReportStatus.PENDING, null);
        report.setDescription("举报描述");
        when(reportMapper.selectById(1)).thenReturn(report);

        assertThat(reportService.getReportDetail(null)).isNull();
        assertThat(reportService.getReportDetail(1).getDescription()).isEqualTo("举报描述");
    }

    private Report report(Integer reportId, ReportStatus status, Integer handlerAccountId) {
        Report report = new Report();
        report.setReportId(reportId);
        report.setReporterAccountId(1);
        report.setReportedAccountId(2);
        report.setTargetType(ReportTargetType.USER);
        report.setTargetId(2);
        report.setReportType("OTHER");
        report.setDescription("举报描述");
        report.setStatus(status);
        report.setHandlerAccountId(handlerAccountId);
        return report;
    }

    private ReportHandleDTO dto(ReportStatus status, String note, AccountAction action) {
        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setStatus(status);
        dto.setHandleNote(note);
        dto.setAccountAction(action);
        return dto;
    }
}
