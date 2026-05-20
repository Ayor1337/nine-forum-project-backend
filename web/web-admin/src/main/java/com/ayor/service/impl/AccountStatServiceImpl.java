package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.entity.vo.AccountStatVO;
import com.ayor.mapper.AccountStatMapper;
import com.ayor.service.AccountStatService;
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
public class AccountStatServiceImpl extends ServiceImpl<AccountStatMapper, AccountStat> implements AccountStatService {

    /**
     * 分页查询用户统计记录，可按账号 ID 过滤。
     */
    @Override
    public PageEntity<AccountStatVO> getAccountStats(Integer pageNum, Integer pageSize, Integer accountId) {
        Page<AccountStat> page = this.lambdaQuery()
                .eq(accountId != null, AccountStat::getAccountId, accountId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public AccountStatVO getAccountStatById(Integer statId) {
        if (statId == null) {
            return null;
        }
        return toVO(this.getById(statId));
    }

    @Override
    public String createAccountStat(AccountStat accountStat) {
        if (accountStat == null || accountStat.getAccountId() == null) {
            return "用户不存在";
        }
        return this.save(accountStat) ? null : "创建统计失败";
    }

    /**
     * 仅更新管理端传入的非空统计字段，避免把未填写项覆盖掉原值。
     */
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
        if (accountStat.getFollowingCount() != null) {
            exist.setFollowingCount(accountStat.getFollowingCount());
        }
        if (accountStat.getFollowerCount() != null) {
            exist.setFollowerCount(accountStat.getFollowerCount());
        }
        if (accountStat.getAccountId() != null) {
            exist.setAccountId(accountStat.getAccountId());
        }
        return this.updateById(exist) ? null : "更新统计失败";
    }

    @Override
    public String deleteAccountStat(Integer statId) {
        if (statId == null) {
            return "统计记录不存在";
        }
        return this.removeById(statId) ? null : "删除统计失败";
    }

    private List<AccountStatVO> toVOList(List<AccountStat> accountStats) {
        List<AccountStatVO> accountStatVOS = new ArrayList<>();
        for (AccountStat accountStat : accountStats) {
            accountStatVOS.add(toVO(accountStat));
        }
        return accountStatVOS;
    }

    private AccountStatVO toVO(AccountStat accountStat) {
        if (accountStat == null) {
            return null;
        }
        AccountStatVO accountStatVO = new AccountStatVO();
        BeanUtils.copyProperties(accountStat, accountStatVO);
        return accountStatVO;
    }
}
