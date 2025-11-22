package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Collect;
import com.baomidou.mybatisplus.extension.service.IService;

public interface CollectService extends IService<Collect> {

    PageEntity<Collect> getCollects(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId);

    String deleteCollect(Integer collectId);
}
