package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Collect;
import com.ayor.entity.vo.CollectVO;
import com.ayor.mapper.CollectMapper;
import com.ayor.service.CollectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    /**
     * 分页查询收藏记录，可按帖子和账号组合过滤。
     */
    @Override
    public PageEntity<CollectVO> getCollects(Integer pageNum, Integer pageSize, Integer threadId, Integer accountId) {
        Page<Collect> page = this.lambdaQuery()
                .eq(threadId != null, Collect::getThreadId, threadId)
                .eq(accountId != null, Collect::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public CollectVO getCollectById(Integer collectId) {
        if (collectId == null) {
            return null;
        }
        return toVO(this.getById(collectId));
    }

    @Override
    public String createCollect(Collect collect) {
        if (collect == null || collect.getThreadId() == null || collect.getAccountId() == null) {
            return "收藏记录参数不完整";
        }
        return this.save(collect) ? null : "创建收藏失败";
    }

    @Override
    public String updateCollect(Collect collect) {
        if (collect == null || collect.getCollectId() == null) {
            return "收藏记录不存在";
        }
        Collect exist = this.getById(collect.getCollectId());
        if (exist == null) {
            return "收藏记录不存在";
        }
        if (collect.getAccountId() != null) {
            exist.setAccountId(collect.getAccountId());
        }
        if (collect.getThreadId() != null) {
            exist.setThreadId(collect.getThreadId());
        }
        return this.updateById(exist) ? null : "更新收藏失败";
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

    private List<CollectVO> toVOList(List<Collect> collects) {
        List<CollectVO> collectVOS = new ArrayList<>();
        for (Collect collect : collects) {
            collectVOS.add(toVO(collect));
        }
        return collectVOS;
    }

    private CollectVO toVO(Collect collect) {
        if (collect == null) {
            return null;
        }
        CollectVO collectVO = new CollectVO();
        BeanUtils.copyProperties(collect, collectVO);
        return collectVO;
    }
}

