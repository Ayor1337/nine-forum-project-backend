package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Collect;
import com.ayor.mapper.CollectMapper;
import com.ayor.service.CollectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    /**
     * 分页查询收藏记录，可按帖子和账号组合过滤。
     */
    @Override
    public PageEntity<Collect> getCollects(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId) {
        Page<Collect> page = this.lambdaQuery()
                .eq(threadId != null, Collect::getThreadId, threadId)
                .eq(accountId != null, Collect::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    /**
     * 删除指定收藏记录。
     */
    @Override
    public String deleteCollect(Integer collectId) {
        if (collectId == null) {
            return "收藏记录不存在";
        }
        return this.removeById(collectId) ? null : "删除收藏失败";
    }
}

