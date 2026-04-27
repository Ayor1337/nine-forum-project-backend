package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ChatboardHistory;
import com.ayor.mapper.ChatboardHistoryMapper;
import com.ayor.service.ChatboardHistoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatboardHistoryServiceImpl extends ServiceImpl<ChatboardHistoryMapper, ChatboardHistory> implements ChatboardHistoryService {

    /**
     * 分页查询聊天板历史记录，可按话题过滤。
     */
    @Override
    public PageEntity<ChatboardHistory> getHistories(Integer topicId, Integer pageNum, Integer pageSize) {
        Page<ChatboardHistory> page = this.lambdaQuery()
                .eq(topicId != null, ChatboardHistory::getTopicId, topicId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
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
}
