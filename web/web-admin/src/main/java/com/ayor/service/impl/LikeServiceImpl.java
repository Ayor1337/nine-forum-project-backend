package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.mapper.LikeMapper;
import com.ayor.service.LikeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeServiceImpl extends ServiceImpl<LikeMapper, LikeThread> implements LikeService {


    @Override
    public PageEntity<LikeThread> getLikes(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId) {
        Page<LikeThread> page = this.lambdaQuery()
                .eq(threadId != null, LikeThread::getThreadId, threadId)
                .eq(accountId != null, LikeThread::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public String deleteLike(Integer likeId) {
        if (likeId == null) {
            return "点赞记录不存在";
        }
        return this.removeById(likeId) ? null : "删除点赞失败";
    }
}
