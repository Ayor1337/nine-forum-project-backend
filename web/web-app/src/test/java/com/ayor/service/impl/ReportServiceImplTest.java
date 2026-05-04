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
import com.ayor.type.ContentReportType;
import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import com.ayor.type.UserReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ThreaddMapper threaddMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void shouldRejectDuplicatePendingThreadReportBeforeSendingMessage() {
        ContentReportDTO dto = new ContentReportDTO();
        dto.setType(ContentReportType.SPAM_ADVERTISING);
        dto.setDescription("这是一个重复举报的测试描述");
        Report pending = new Report();
        pending.setReportId(7);
        pending.setStatus(ReportStatus.PENDING);

        doReturn(pending).when(reportService).findActiveReport(3, ReportTargetType.THREAD, 11);

        String result = reportService.createThreadReport(3, 11, dto);

        assertEquals("请勿重复举报，当前举报正在处理中", result);
        verify(reportService, never()).sendReportCreatedMessage(any());
    }

    @Test
    void shouldSendThreadReportMessageWhenRequestIsValid() {
        ContentReportDTO dto = new ContentReportDTO();
        dto.setType(ContentReportType.ABUSE_HARASSMENT);
        dto.setDescription("该帖子包含持续的人身攻击内容，影响讨论秩序。");

        Threadd thread = new Threadd();
        thread.setThreadId(11);
        thread.setTitle("违规帖子");
        thread.setContent("违规内容正文");
        thread.setAccountId(8);

        Account reportedAccount = new Account();
        reportedAccount.setAccountId(8);
        reportedAccount.setUsername("reported-user");
        reportedAccount.setNickname("被举报用户");
        reportedAccount.setCreateTime(new Date());
        reportedAccount.setUpdateTime(new Date());

        doReturn(null).when(reportService).findActiveReport(3, ReportTargetType.THREAD, 11);
        when(threaddMapper.selectById(11)).thenReturn(thread);
        when(accountMapper.getAccountById(8)).thenReturn(reportedAccount);

        String result = reportService.createThreadReport(3, 11, dto);

        assertNull(result);
        ArgumentCaptor<ReportCreatedMessage> captor = ArgumentCaptor.forClass(ReportCreatedMessage.class);
        verify(reportService).sendReportCreatedMessage(captor.capture());
        ReportCreatedMessage message = captor.getValue();
        assertEquals(3, message.getReporterAccountId());
        assertEquals(8, message.getReportedAccountId());
        assertEquals(ReportTargetType.THREAD, message.getTargetType());
        assertEquals(11, message.getTargetId());
        assertEquals(ContentReportType.ABUSE_HARASSMENT.name(), message.getReportType());
        assertEquals("被举报用户", message.getReportedUsernameSnapshot());
        assertEquals("违规帖子", message.getTargetSummarySnapshot());
    }

    @Test
    void shouldRejectSelfUserReport() {
        UserReportDTO dto = new UserReportDTO();
        dto.setType(UserReportType.IMPERSONATION);
        dto.setDescription("用户正在冒充其他人。");

        String result = reportService.createUserReport(5, 5, dto);

        assertEquals("不能举报自己", result);
        verify(reportService, never()).sendReportCreatedMessage(any());
    }

    @Test
    void shouldSendPostReportMessageWhenRequestIsValid() {
        ContentReportDTO dto = new ContentReportDTO();
        dto.setType(ContentReportType.ILLEGAL_CONTENT);
        dto.setDescription("评论中存在明显违法违规内容，需要管理员介入。");

        Post post = new Post();
        post.setPostId(13);
        post.setContent("违法评论内容");
        post.setThreadId(11);
        post.setAccountId(9);

        Account reportedAccount = new Account();
        reportedAccount.setAccountId(9);
        reportedAccount.setUsername("post-owner");
        reportedAccount.setNickname("评论作者");
        reportedAccount.setCreateTime(new Date());
        reportedAccount.setUpdateTime(new Date());

        doReturn(null).when(reportService).findActiveReport(2, ReportTargetType.POST, 13);
        when(postMapper.selectById(13)).thenReturn(post);
        when(accountMapper.getAccountById(9)).thenReturn(reportedAccount);

        String result = reportService.createPostReport(2, 13, dto);

        assertNull(result);
        ArgumentCaptor<ReportCreatedMessage> captor = ArgumentCaptor.forClass(ReportCreatedMessage.class);
        verify(reportService).sendReportCreatedMessage(captor.capture());
        assertEquals(ReportTargetType.POST, captor.getValue().getTargetType());
        assertEquals(13, captor.getValue().getTargetId());
        assertEquals("评论作者", captor.getValue().getReportedUsernameSnapshot());
        assertEquals("违法评论内容", captor.getValue().getTargetSummarySnapshot());
    }
}
