package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.entity.vo.ChatboardHistoryVO;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.ChatboardHistoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatboardHistoryServiceImpl extends ServiceImpl<ChatboardHistoryMapper, ChatboardHistory> implements ChatboardHistoryService {

    /**
     * 分页查询聊天板历史记录，可按话题过滤。
     */
    @Override
    public PageEntity<ChatboardHistoryVO> getHistories(Integer topicId, Integer pageNum, Integer pageSize) {
        Page<ChatboardHistory> page = this.lambdaQuery()
                .eq(topicId != null, ChatboardHistory::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public ChatboardHistoryVO getHistoryById(Integer historyId) {
        if (historyId == null) {
            return null;
        }
        return toVO(this.getById(historyId));
    }

    @Override
    public String createHistory(ChatboardHistory history) {
        if (history == null || history.getTopicId() == null || history.getAccountId() == null) {
            return "聊天记录参数不完整";
        }
        if (history.getCreateTime() == null) {
            history.setCreateTime(new Date());
        }
        return this.save(history) ? null : "创建聊天记录失败";
    }

    @Override
    public String updateHistory(ChatboardHistory history) {
        if (history == null || history.getChatboardHistoryId() == null) {
            return "聊天记录不存在";
        }
        ChatboardHistory exist = this.getById(history.getChatboardHistoryId());
        if (exist == null) {
            return "聊天记录不存在";
        }
        if (history.getAccountId() != null) {
            exist.setAccountId(history.getAccountId());
        }
        if (history.getTopicId() != null) {
            exist.setTopicId(history.getTopicId());
        }
        if (history.getContent() != null) {
            exist.setContent(history.getContent());
        }
        if (history.getCreateTime() != null) {
            exist.setCreateTime(history.getCreateTime());
        }
        return this.updateById(exist) ? null : "更新聊天记录失败";
    }

    /**
     * 物理删除指定聊天板历史记录。
     */
    @Override
    public String deleteHistory(Integer historyId) {
        if (historyId == null) {
            return "聊天记录不存在";
        }
        return this.removeById(historyId) ? null : "删除聊天记录失败";
    }

    private List<ChatboardHistoryVO> toVOList(List<ChatboardHistory> histories) {
        List<ChatboardHistoryVO> chatboardHistoryVOS = new ArrayList<>();
        for (ChatboardHistory history : histories) {
            chatboardHistoryVOS.add(toVO(history));
        }
        return chatboardHistoryVOS;
    }

    private ChatboardHistoryVO toVO(ChatboardHistory history) {
        if (history == null) {
            return null;
        }
        ChatboardHistoryVO chatboardHistoryVO = new ChatboardHistoryVO();
        BeanUtils.copyProperties(history, chatboardHistoryVO);
        return chatboardHistoryVO;
    }
}
