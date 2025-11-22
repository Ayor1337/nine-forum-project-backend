package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.service.AccountStatService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountStatServiceImpl extends ServiceImpl<AccountStatMapper, AccountStat> implements AccountStatService {

    @Override
    public PageEntity<AccountStat> getAccountStats(Integer pageNum, Integer pageSize, Integer accountId) {
        Page<AccountStat> page = this.lambdaQuery()
                .eq(accountId != null, AccountStat::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), page.getRecords());
    }

    @Override
    public String updateAccountStat(Integer statId, AccountStat accountStat) {
        if (statId == null) {
            return "统计记录不存在";
        }
        AccountStat exist = this.getById(statId);
        if (exist == null) {
            return "统计记录不存在";
        }
        if (accountStat.getThreadCount() != null) {
            exist.setThreadCount(accountStat.getThreadCount());
        }
        if (accountStat.getPostCount() != null) {
            exist.setPostCount(accountStat.getPostCount());
        }
        if (accountStat.getReplyCount() != null) {
            exist.setReplyCount(accountStat.getReplyCount());
        }
        if (accountStat.getLikedCount() != null) {
            exist.setLikedCount(accountStat.getLikedCount());
        }
        if (accountStat.getCollectedCount() != null) {
            exist.setCollectedCount(accountStat.getCollectedCount());
        }
        if (accountStat.getAccountId() != null) {
            exist.setAccountId(accountStat.getAccountId());
        }
        return this.updateById(exist) ? null : "更新统计失败";
    }
}
