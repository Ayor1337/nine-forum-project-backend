package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.vo.ThreadTableVO;
import com.ayor.entity.pojo.Threadd;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ThreaddService extends IService<Threadd> {
    PageEntity<ThreadTableVO> getThreads(Integer pageNum, Integer pageSize);
}
