package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.LikeThread;
import com.baomidou.mybatisplus.extension.service.IService;


public interface LikeThreadService extends IService<LikeThread> {
    String insertLikeThreadId(String username, Integer threadId);

    String removeLikeThreadId(String username, Integer threadId);

    PageEntity<ThreadVO> getLikesByAccountId(Integer accountId, Integer currentPage, Integer pageSize);

    Integer getLikeCountByThreadId(Integer threadId);

    Boolean isLikedByUsername(String username, Integer threadId);
}
