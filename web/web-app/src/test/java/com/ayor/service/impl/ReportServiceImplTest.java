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
import com.ayor.type.ReportTargetType;
import com.ayor.type.UserReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
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

    // 测试创建用户举报拒绝缺失举报人并自举报
    @Test
    void createUserReportRejectsMissingReporterAndSelfReport() {
        ReportServiceImpl service = createService();
        UserReportDTO dto = userReportDto("这是一段足够长的举报描述");

        assertThat(service.createUserReport(null, 2, dto)).isEqualTo("用户不存在");
        assertThat(service.createUserReport(2, 2, dto)).isEqualTo("不能举报自己");

        verify(rabbitTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<Object>any());
    }

    // 测试创建用户举报拒绝无效描述查询前
    @Test
    void createUserReportRejectsInvalidDescriptionBeforeLookup() {
        ReportServiceImpl service = createService();
        UserReportDTO dto = userReportDto("太短");

        String result = service.createUserReport(1, 2, dto);

        assertThat(result).isEqualTo("举报描述长度应在 10 到 500 个字符之间");
        verify(accountMapper, never()).getAccountById(org.mockito.ArgumentMatchers.anyInt());
    }

    // 测试创建用户举报拒绝重复有效举报
    @Test
    void createUserReportRejectsDuplicateActiveReport() {
        ReportServiceImpl service = createService();
        when(reportMapper.selectActiveReport(1, ReportTargetType.USER.name(), 2)).thenReturn(new Report());

        String result = service.createUserReport(1, 2, userReportDto("这是一段足够长的举报描述"));

        assertThat(result).isEqualTo("请勿重复举报，当前举报正在处理中");
        verify(rabbitTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<Object>any());
    }

    // 测试创建用户举报发送消息带有快照
    @Test
    void createUserReportSendsMessageWithSnapshots() {
        ReportServiceImpl service = createService();
        Account reported = new Account();
        reported.setAccountId(2);
        reported.setUsername("reported-user");
        reported.setNickname("reported-nick");
        when(accountMapper.getAccountById(2)).thenReturn(reported);

        String result = service.createUserReport(1, 2, userReportDto("  这是一段足够长的举报描述  "));

        ArgumentCaptor<ReportCreatedMessage> captor = ArgumentCaptor.forClass(ReportCreatedMessage.class);
        verify(rabbitTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("report.direct"),
                org.mockito.ArgumentMatchers.eq("report.created"), captor.capture());
        assertThat(result).isNull();
        assertThat(captor.getValue()).satisfies(message -> {
            assertThat(message.getReporterAccountId()).isEqualTo(1);
            assertThat(message.getReportedAccountId()).isEqualTo(2);
            assertThat(message.getTargetType()).isEqualTo(ReportTargetType.USER);
            assertThat(message.getReportType()).isEqualTo(UserReportType.HARASSMENT.name());
            assertThat(message.getDescription()).isEqualTo("这是一段足够长的举报描述");
            assertThat(message.getReportedUsernameSnapshot()).isEqualTo("reported-nick");
            assertThat(message.getTargetSummarySnapshot()).isEqualTo("reported-nick");
        });
    }

    // 测试创建帖子串举报拒绝已删除帖子串并自举报
    @Test
    void createThreadReportRejectsDeletedThreadAndSelfReport() {
        ReportServiceImpl service = createService();
        Threadd deleted = thread(2, "标题", "正文");
        deleted.setIsDeleted(true);
        when(threaddMapper.selectById(10)).thenReturn(deleted);
        when(threaddMapper.selectById(11)).thenReturn(thread(1, "标题", "正文"));

        assertThat(service.createThreadReport(1, 10, contentReportDto("这是一段足够长的举报描述")))
                .isEqualTo("帖子不存在");
        assertThat(service.createThreadReport(1, 11, contentReportDto("这是一段足够长的举报描述")))
                .isEqualTo("不能举报自己");
    }

    // 测试创建帖子串举报发送帖子串摘要快照
    @Test
    void createThreadReportSendsThreadSummarySnapshot() {
        ReportServiceImpl service = createService();
        when(threaddMapper.selectById(10)).thenReturn(thread(2, "很长的帖子标题", "正文"));
        Account reported = new Account();
        reported.setUsername("thread-owner");
        when(accountMapper.getAccountById(2)).thenReturn(reported);

        String result = service.createThreadReport(1, 10, contentReportDto("这是一段足够长的举报描述"));

        ArgumentCaptor<ReportCreatedMessage> captor = ArgumentCaptor.forClass(ReportCreatedMessage.class);
        verify(rabbitTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("report.direct"),
                org.mockito.ArgumentMatchers.eq("report.created"), captor.capture());
        assertThat(result).isNull();
        assertThat(captor.getValue().getTargetType()).isEqualTo(ReportTargetType.THREAD);
        assertThat(captor.getValue().getTargetSummarySnapshot()).isEqualTo("很长的帖子标题");
        assertThat(captor.getValue().getReportedUsernameSnapshot()).isEqualTo("thread-owner");
    }

    // 测试创建帖子举报拒绝已删除帖子并自举报
    @Test
    void createPostReportRejectsDeletedPostAndSelfReport() {
        ReportServiceImpl service = createService();
        Post deleted = post(2, "评论内容");
        deleted.setIsDeleted(true);
        when(postMapper.selectById(10)).thenReturn(deleted);
        when(postMapper.selectById(11)).thenReturn(post(1, "评论内容"));

        assertThat(service.createPostReport(1, 10, contentReportDto("这是一段足够长的举报描述")))
                .isEqualTo("评论不存在");
        assertThat(service.createPostReport(1, 11, contentReportDto("这是一段足够长的举报描述")))
                .isEqualTo("不能举报自己");
    }

    // 测试创建帖子举报截断帖子摘要快照
    @Test
    void createPostReportTruncatesPostSummarySnapshot() {
        ReportServiceImpl service = createService();
        String longContent = "x".repeat(80);
        when(postMapper.selectById(10)).thenReturn(post(2, longContent));
        Account reported = new Account();
        reported.setNickname("post-owner");
        when(accountMapper.getAccountById(2)).thenReturn(reported);

        String result = service.createPostReport(1, 10, contentReportDto("这是一段足够长的举报描述"));

        ArgumentCaptor<ReportCreatedMessage> captor = ArgumentCaptor.forClass(ReportCreatedMessage.class);
        verify(rabbitTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("report.direct"),
                org.mockito.ArgumentMatchers.eq("report.created"), captor.capture());
        assertThat(result).isNull();
        assertThat(captor.getValue().getTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(captor.getValue().getTargetSummarySnapshot()).hasSize(60);
    }

    private ReportServiceImpl createService() {
        return new ReportServiceImpl(reportMapper, accountMapper, threaddMapper, postMapper, rabbitTemplate);
    }

    private UserReportDTO userReportDto(String description) {
        UserReportDTO dto = new UserReportDTO();
        dto.setType(UserReportType.HARASSMENT);
        dto.setDescription(description);
        return dto;
    }

    private ContentReportDTO contentReportDto(String description) {
        ContentReportDTO dto = new ContentReportDTO();
        dto.setType(ContentReportType.SPAM_ADVERTISING);
        dto.setDescription(description);
        return dto;
    }

    private Threadd thread(Integer accountId, String title, String content) {
        Threadd thread = new Threadd();
        thread.setAccountId(accountId);
        thread.setTitle(title);
        thread.setContent(content);
        thread.setIsDeleted(false);
        return thread;
    }

    private Post post(Integer accountId, String content) {
        Post post = new Post();
        post.setAccountId(accountId);
        post.setContent(content);
        post.setIsDeleted(false);
        return post;
    }
}
