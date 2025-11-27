package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Collect;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CollectService extends IService<Collect> {

    String insertCollect(Integer accountId, Integer threadId);

    String removeCollect(Integer accountId, Integer threadId);

    Boolean isCollectedByAccountId(Integer accountId, Integer threadId);

    Integer getCollectCountByThreadId(Integer threadId);

    PageEntity<ThreadVO> getCollectsByAccountId(Integer accountId, Integer pageNum, Integer pageSize);
}
