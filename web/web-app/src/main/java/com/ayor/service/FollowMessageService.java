package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.FollowMessage;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.vo.FollowMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FollowMessageService extends IService<FollowMessage> {

    void createThreadFollowMessages(Threadd thread);

    PageEntity<FollowMessageVO> listFollowMessages(Integer pageNum, Integer pageSize, Integer accountId);
}
