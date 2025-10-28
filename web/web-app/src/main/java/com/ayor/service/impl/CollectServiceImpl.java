package com.ayor.service.impl;

import com.ayor.entity.app.vo.ThreadPageVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Collect;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.CollectMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.CollectService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    private final AccountMapper accountMapper;

    private final ThreaddMapper threaddMapper;

    @Override
    public String insertCollect(String username, Integer threadId) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            return "用户不存在";
        }
        if (isCollectedByUsername(username, threadId)) {
            return "不能重复收藏";
        }
        Collect collect = new Collect();
        collect.setThreadId(threadId);
        collect.setAccountId(account.getAccountId());
        return this.save(collect) ? null : "收藏失败";
    }

    @Override
    public String removeCollect(String username, Integer threadId) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null)
            return "取消收藏失败";
        Collect collect = this.lambdaQuery().eq(Collect::getAccountId, account.getAccountId())
                .eq(Collect::getThreadId, threadId)
                .one();
        if (collect == null) {
            return "不能取消未收藏的内容";
        }
        return this.removeById(collect) ? null : "取消收藏失败";
    }

    @Override
    public Boolean isCollectedByUsername(String username, Integer threadId) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        return this.lambdaQuery()
                .eq(Collect::getAccountId, account.getAccountId())
                .eq(Collect::getThreadId, threadId).count() > 0;
    }

    @Override
    public Integer getCollectCountByThreadId(Integer threadId) {
        Integer collectCountByThreadId = this.baseMapper.getCollectCountByThreadId(threadId);
        return collectCountByThreadId == null ? 0 : collectCountByThreadId;
    }

    @Override
    public ThreadPageVO getCollectsByAccountId(Integer accountId, Integer pageNum, Integer pageSize) {
        Page<Collect> page = new Page<>(pageNum, pageSize);
        List<Collect> collects = this.lambdaQuery().eq(Collect::getAccountId, accountId)
                .page(page).getRecords();
        List<Integer> threadIds = new ArrayList<>();
        collects.forEach(collect -> threadIds.add(collect.getThreadId()));
        List<Threadd> threads = threaddMapper.selectByIds(threadIds);
        List<ThreadVO> threadVOS = new ArrayList<>();
        threads.forEach(thread -> {
            ThreadVO threadVO = new ThreadVO();
            BeanUtils.copyProperties(thread, threadVO);
            threadVO.setContent(null);
            threadVO.setImageUrls(null);
            threadVOS.add(threadVO);
        });
        ThreadPageVO threadPageVO = new ThreadPageVO((int) page.getTotal(), threadVOS);
        return threadPageVO;
    }
}

