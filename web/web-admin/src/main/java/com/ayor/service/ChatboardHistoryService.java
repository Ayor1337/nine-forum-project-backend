package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ChatboardHistory;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ChatboardHistoryService extends IService<ChatboardHistory> {

    PageEntity<ChatboardHistory> getHistories(Integer topicId, Integer pageNum, Integer pageSize);

    String deleteHistory(Integer historyId);
}
