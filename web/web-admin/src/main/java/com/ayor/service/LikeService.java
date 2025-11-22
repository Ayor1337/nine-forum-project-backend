package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.LikeThread;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LikeService extends IService<LikeThread> {

    PageEntity<LikeThread> getLikes(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId);

    String deleteLike(Integer likeId);
}
