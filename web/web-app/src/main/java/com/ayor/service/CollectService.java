package com.ayor.service;

import com.ayor.entity.app.vo.CollectVO;
import com.ayor.entity.app.vo.ThreadPageVO;
import com.ayor.entity.pojo.Collect;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface CollectService extends IService<Collect> {

    String insertCollect(String username, Integer threadId);

    String removeCollect(String username, Integer threadId);

    Boolean isCollectedByUsername(String username, Integer threadId);

    Integer getCollectCountByThreadId(Integer threadId);

    ThreadPageVO getCollectsByAccountId(Integer accountId, Integer pageNum, Integer pageSize);
}
