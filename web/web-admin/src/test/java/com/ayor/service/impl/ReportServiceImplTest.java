package com.ayor.service.impl;

import com.ayor.entity.dto.ReportHandleDTO;
import com.ayor.entity.message.ReportCreatedMessage;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.pojo.Report;
import com.ayor.entity.stomp.ReportStompMessage;
import com.ayor.mapper.ReportMapper;
import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
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

    @Spy
    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void shouldPersistIncomingMessageAndBroadcastToAdmins() {
        ReportCreatedMessage message = ReportCreatedMessage.builder()
                .reporterAccountId(3)
                .reportedAccountId(8)
                .targetType(ReportTargetType.THREAD)
                .targetId(11)
                .reportType("ABUSE_HARASSMENT")
                .description("举报描述内容，长度已经满足要求。")
                .reportedUsernameSnapshot("被举报用户")
                .targetSummarySnapshot("违规帖子")
                .build();

        when(reportMapper.insert(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setReportId(21);
            return 1;
        });

        reportService.createFromMessage(message);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportMapper).insert(reportCaptor.capture());
        Report saved = reportCaptor.getValue();
        assertEquals(ReportStatus.PENDING, saved.getStatus());
        assertEquals(3, saved.getReporterAccountId());
        assertEquals("违规帖子", saved.getTargetSummarySnapshot());

        ArgumentCaptor<ReportStompMessage> stompCaptor = ArgumentCaptor.forClass(ReportStompMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/reports"), stompCaptor.capture());
        assertEquals(21, stompCaptor.getValue().getReportId());
        assertEquals(ReportStatus.PENDING, stompCaptor.getValue().getStatus());
    }

    @Test
    void shouldRejectFinalHandlingByAnotherAdmin() {
        Report report = new Report();
        report.setReportId(9);
        report.setStatus(ReportStatus.PROCESSING);
        report.setHandlerAccountId(12);

        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setStatus(ReportStatus.RESOLVED);
        dto.setHandleNote("已经完成处理");

        doReturn(report).when(reportService).getById(9);

        String result = reportService.handleReport(9, 18, dto);

        assertEquals("该举报已由其他管理员接手", result);
        verify(reportService, never()).sendReportResultMessage(any());
    }

    @Test
    void shouldResolveReportAndNotifyReporter() {
        Report report = new Report();
        report.setReportId(9);
        report.setReporterAccountId(3);
        report.setStatus(ReportStatus.PENDING);
        report.setReportedUsernameSnapshot("被举报用户");
        report.setTargetSummarySnapshot("违规帖子");
        report.setCreateTime(new Date());

        ReportHandleDTO dto = new ReportHandleDTO();
        dto.setStatus(ReportStatus.RESOLVED);
        dto.setHandleNote("已确认违规并完成处理");

        doReturn(report).when(reportService).getById(9);
        doReturn(true).when(reportService).updateById(report);

        String result = reportService.handleReport(9, 12, dto);

        assertNull(result);
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertEquals(12, report.getHandlerAccountId());
        assertEquals("已确认违规并完成处理", report.getHandleNote());
        ArgumentCaptor<UserSystemMessage> captor = ArgumentCaptor.forClass(UserSystemMessage.class);
        verify(reportService).sendReportResultMessage(captor.capture());
        assertEquals(3, captor.getValue().getSendTo());
    }
}
