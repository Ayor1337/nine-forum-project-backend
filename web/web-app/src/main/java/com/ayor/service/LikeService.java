package com.ayor.service;

import com.ayor.entity.app.vo.ThreadPageVO;
import com.ayor.entity.pojo.Like;
import com.baomidou.mybatisplus.extension.service.IService;


public interface LikeService extends IService<Like> {
    String insertLikeThreadId(String username, Integer threadId);

    String removeLikeThreadId(String username, Integer threadId);

    ThreadPageVO getLikesByAccountId(Integer accountId, Integer currentPage, Integer pageSize);

    Integer getLikeCountByThreadId(Integer threadId);

    Boolean isLikedByUsername(String username, Integer threadId);
}
