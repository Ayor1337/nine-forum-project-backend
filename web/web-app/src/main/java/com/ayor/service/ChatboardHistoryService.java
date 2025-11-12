package com.ayor.service;

import com.ayor.entity.app.vo.ChatboardHistoryVO;
import com.ayor.entity.pojo.ChatboardHistory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ChatboardHistoryService extends IService<ChatboardHistory> {
    String insertChatboardHistory(Integer accountId,
                                  Integer topicId,
                                  String content);

    List<ChatboardHistoryVO> getChatboardHistory(Integer topicId);
}
