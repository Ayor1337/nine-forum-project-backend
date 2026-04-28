package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Collect;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.CollectMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.CollectService;
import com.ayor.service.PrivacyPolicyService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    // 收藏数据写入频繁且需要与帖子状态同步，这里暂不启用缓存。

    private final AccountMapper accountMapper;

    private final ThreaddMapper threaddMapper;

    private final PrivacyPolicyService privacyPolicyService;
    /**
     * 为当前用户收藏指定帖子，重复收藏会被拒绝。
     */

    @Override
    public String insertCollect(Integer accountId, Integer threadId) {
        // TODO 为什么不能查看，但是可以收藏呢？这权限管理写的牛魔，傻逼 gpt 5.4
        if (accountId == null || threadId == null) {
            return "参数错误";
        }
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        if (isCollectedByAccountId(accountId, threadId)) {
            return "不能重复收藏";
        }
        Collect collect = new Collect();
        collect.setThreadId(threadId);
        collect.setAccountId(account.getAccountId());
        return this.save(collect) ? null : "收藏失败";
    }
    /**
     * 取消当前用户对指定帖子的收藏。
     */

    @Override
    public String removeCollect(Integer accountId, Integer threadId) {
        if (accountId == null || threadId == null) {
            return "参数错误";
        }
        Collect collect = this.lambdaQuery().eq(Collect::getAccountId, accountId)
                .eq(Collect::getThreadId, threadId)
                .one();
        if (collect == null) {
            return "不能取消未收藏的内容";
        }
        return this.removeById(collect) ? null : "取消收藏失败";
    }
    /**
     * 判断用户是否已经收藏指定帖子。
     */

    @Override
    public Boolean isCollectedByAccountId(Integer accountId, Integer threadId) {
        if (accountId == null) {
            return false;
        }
        return this.lambdaQuery()
                .eq(Collect::getAccountId, accountId)
                .eq(Collect::getThreadId, threadId).count() > 0;
    }
    /**
     * 获取指定帖子的收藏数。
     */

    @Override
    public Integer getCollectCountByThreadId(Integer threadId) {
        Integer collectCountByThreadId = this.baseMapper.getCollectCountByThreadId(threadId);
        return collectCountByThreadId == null ? 0 : collectCountByThreadId;
    }
    /**
     * 分页获取用户收藏的帖子列表。
     *
     * @param viewerId 当前查看者用户ID
     * @param accountId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含用户收藏的帖子视图对象列表
     */
    @Override
    public PageEntity<ThreadVO> getCollectsByAccountId(Integer viewerId, Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountMapper.getAccountById(accountId) == null) {
            return null;
        }
        if (!privacyPolicyService.canViewCollectedThreads(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看收藏列表");
        }
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
        return new PageEntity<>(page.getTotal(), threadVOS);
    }
}

