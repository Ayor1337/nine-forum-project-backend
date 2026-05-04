package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.History;
import com.ayor.entity.vo.HistoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface HistoryService extends IService<History> {

    PageEntity<HistoryVO> getHistories(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId);

    HistoryVO getHistoryById(Integer historyId);

    String createHistory(History history);

    String updateHistory(History history);

    String deleteHistory(Integer historyId);
}
