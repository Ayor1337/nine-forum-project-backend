package com.ayor.service.impl;

import com.ayor.entity.pojo.AccountStat;
import com.ayor.entity.pojo.History;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.pojo.Theme;
import com.ayor.entity.pojo.TopicChat;
import com.ayor.entity.pojo.TopicStat;
import com.ayor.entity.vo.AccountStatVO;
import com.ayor.entity.vo.HistoryVO;
import com.ayor.entity.vo.LikeThreadVO;
import com.ayor.entity.vo.TagVO;
import com.ayor.entity.vo.ThemeVO;
import com.ayor.entity.vo.TopicChatVO;
import com.ayor.entity.vo.TopicStatVO;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.mapper.HistoryMapper;
import com.ayor.mapper.LikeMapper;
import com.ayor.mapper.TagMapper;
import com.ayor.mapper.ThemeMapper;
import com.ayor.mapper.TopicChatMapper;
import com.ayor.mapper.TopicStatMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCrudServiceImplTest {

    @Mock
    private AccountStatMapper accountStatMapper;

    @Mock
    private TopicStatMapper topicStatMapper;

    @Mock
    private HistoryMapper historyMapper;

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ThemeMapper themeMapper;

    @Mock
    private TopicChatMapper topicChatMapper;

    // 测试账号统计服务校验创建并合并部分更新
    @Test
    void accountStatServiceValidatesCreateAndMergesPartialUpdates() {
        AccountStatServiceImpl service = new AccountStatServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", accountStatMapper);
        AccountStat existing = new AccountStat();
        existing.setUserStatId(3);
        existing.setAccountId(7);
        existing.setThreadCount(1);
        AccountStat patch = new AccountStat();
        patch.setThreadCount(9);
        patch.setPostCount(2);
        when(accountStatMapper.selectById(3)).thenReturn(existing);
        when(accountStatMapper.updateById(existing)).thenReturn(1);

        assertThat(service.createAccountStat(new AccountStat())).isEqualTo("用户不存在");
        assertThat(service.updateAccountStat(3, patch)).isNull();
        assertThat(existing.getThreadCount()).isEqualTo(9);
        assertThat(existing.getPostCount()).isEqualTo(2);
        assertThat(existing.getAccountId()).isEqualTo(7);
    }

    // 测试账号统计服务映射VO并返回缺失记录消息
    @Test
    void accountStatServiceMapsEntityToVoAndReportsMissingRecords() {
        AccountStatServiceImpl service = new AccountStatServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", accountStatMapper);
        AccountStat stat = new AccountStat();
        stat.setUserStatId(3);
        stat.setAccountId(7);
        stat.setThreadCount(4);

        AccountStatVO vo = ReflectionTestUtils.invokeMethod(service, "toVO", stat);

        assertThat(vo.getUserStatId()).isEqualTo(3);
        assertThat(vo.getAccountId()).isEqualTo(7);
        assertThat(service.updateAccountStat(null, stat)).isEqualTo("统计记录不存在");
        assertThat(service.deleteAccountStat(null)).isEqualTo("统计记录不存在");
    }

    // 测试主题统计服务校验并合并更新
    @Test
    void topicStatServiceValidatesAndMergesUpdates() {
        TopicStatServiceImpl service = new TopicStatServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", topicStatMapper);
        TopicStat existing = new TopicStat();
        existing.setTopicStatId(5);
        existing.setTopicId(9);
        existing.setThreadCount(1);
        TopicStat patch = new TopicStat();
        patch.setViewCount(100);
        when(topicStatMapper.selectById(5)).thenReturn(existing);
        when(topicStatMapper.updateById(existing)).thenReturn(1);

        assertThat(service.createTopicStat(new TopicStat())).isEqualTo("话题不存在");
        assertThat(service.updateTopicStat(5, patch)).isNull();
        assertThat(existing.getTopicId()).isEqualTo(9);
        assertThat(existing.getViewCount()).isEqualTo(100);

        TopicStatVO vo = ReflectionTestUtils.invokeMethod(service, "toVO", existing);
        assertThat(vo.getTopicStatId()).isEqualTo(5);
    }

    // 测试历史服务初始化创建时间并合并更新
    @Test
    void historyServiceInitializesCreateTimeAndMergesUpdates() {
        HistoryServiceImpl service = new HistoryServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", historyMapper);
        History existing = new History();
        existing.setHistoryId(8);
        existing.setThreadId(1);
        existing.setAccountId(7);
        History patch = new History();
        patch.setHistoryId(8);
        patch.setThreadId(2);
        when(historyMapper.insert(any(History.class))).thenReturn(1);
        when(historyMapper.selectById(8)).thenReturn(existing);
        when(historyMapper.updateById(existing)).thenReturn(1);

        History created = new History();
        created.setThreadId(1);
        created.setAccountId(7);
        assertThat(service.createHistory(created)).isNull();
        assertThat(created.getCreateTime()).isNotNull();
        assertThat(service.updateHistory(patch)).isNull();
        assertThat(existing.getThreadId()).isEqualTo(2);

        HistoryVO vo = ReflectionTestUtils.invokeMethod(service, "toVO", existing);
        assertThat(vo.getHistoryId()).isEqualTo(8);
    }

    // 测试点赞服务校验并合并更新
    @Test
    void likeServiceValidatesAndMergesUpdates() {
        LikeServiceImpl service = new LikeServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", likeMapper);
        LikeThread existing = new LikeThread();
        existing.setLikeId(4);
        existing.setAccountId(7);
        existing.setThreadId(1);
        LikeThread patch = new LikeThread();
        patch.setLikeId(4);
        patch.setThreadId(2);
        when(likeMapper.selectById(4)).thenReturn(existing);
        when(likeMapper.updateById(existing)).thenReturn(1);

        assertThat(service.createLike(new LikeThread())).isEqualTo("点赞记录参数不完整");
        assertThat(service.updateLike(patch)).isNull();
        assertThat(existing.getAccountId()).isEqualTo(7);
        assertThat(existing.getThreadId()).isEqualTo(2);

        LikeThreadVO vo = ReflectionTestUtils.invokeMethod(service, "toVO", existing);
        assertThat(vo.getLikeId()).isEqualTo(4);
    }

    // 测试标签服务校验创建并更新分支
    @Test
    void tagServiceValidatesCreateAndUpdateBranches() {
        TagServiceImpl service = new TagServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", tagMapper);
        Tag existing = new Tag();
        existing.setTagId(6);
        existing.setTag("old");
        existing.setTopicId(1);
        Tag patch = new Tag();
        patch.setTagId(6);
        patch.setTag("new");
        when(tagMapper.selectById(6)).thenReturn(existing);
        when(tagMapper.updateById(existing)).thenReturn(1);

        assertThat(service.createTag(null)).isEqualTo("标签名称不能为空");
        Tag missingTopic = new Tag();
        missingTopic.setTag("java");
        assertThat(service.createTag(missingTopic)).isEqualTo("请选择所属话题");
        assertThat(service.updateTag(patch)).isNull();
        assertThat(existing.getTag()).isEqualTo("new");

        @SuppressWarnings("unchecked")
        List<TagVO> vos = ReflectionTestUtils.invokeMethod(service, "toVOList", List.of(existing));
        assertThat(vos).singleElement().satisfies(vo -> assertThat(vo.getTagId()).isEqualTo(6));
    }

    // 测试版块服务初始化删除标记并软删除
    @Test
    void themeServiceInitializesDeleteFlagAndSoftDeletes() {
        ThemeServiceImpl service = new ThemeServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", themeMapper);
        Theme existing = new Theme();
        existing.setThemeId(2);
        existing.setTitle("old");
        existing.setIsDeleted(false);
        when(themeMapper.insert(any(Theme.class))).thenReturn(1);
        when(themeMapper.selectById(2)).thenReturn(existing);
        when(themeMapper.updateById(existing)).thenReturn(1);

        com.ayor.entity.dto.ThemeDTO createDto = new com.ayor.entity.dto.ThemeDTO();
        createDto.setTitle("new");
        assertThat(service.createTheme(createDto)).isNull();
        assertThat(service.deleteTheme(2)).isNull();
        assertThat(existing.getIsDeleted()).isTrue();

        ThemeVO vo = ReflectionTestUtils.invokeMethod(service, "toVO", existing);
        assertThat(vo.getThemeId()).isEqualTo(2);
        assertThat(service.createTheme(new com.ayor.entity.dto.ThemeDTO())).isEqualTo("主题名称不能为空");
    }

    // 测试主题聊天服务校验内容并合并更新
    @Test
    void topicChatServiceValidatesContentAndMergesUpdates() {
        TopicChatServiceImpl service = new TopicChatServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", topicChatMapper);
        TopicChat existing = new TopicChat();
        existing.setTopicChatId(11);
        existing.setTopicId(1);
        existing.setAccountId(7);
        existing.setContent("old");
        TopicChat patch = new TopicChat();
        patch.setTopicChatId(11);
        patch.setContent("new");
        when(topicChatMapper.selectById(11)).thenReturn(existing);
        when(topicChatMapper.updateById(existing)).thenReturn(1);

        TopicChat invalid = new TopicChat();
        invalid.setTopicId(1);
        invalid.setAccountId(7);
        assertThat(service.createTopicChat(invalid)).isEqualTo("聊天内容不能为空");
        assertThat(service.updateTopicChat(patch)).isNull();
        assertThat(existing.getContent()).isEqualTo("new");

        TopicChatVO vo = ReflectionTestUtils.invokeMethod(service, "toVO", existing);
        assertThat(vo.getTopicChatId()).isEqualTo(11);
    }
}
