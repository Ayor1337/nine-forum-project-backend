package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.SystemMessageVO;
import com.ayor.entity.pojo.SystemMessage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SystemMessageService extends IService<SystemMessage> {

    PageEntity<SystemMessageVO> listSystemMessage(Integer pageNum, Integer pageSize, String username);
}
