package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.AccountDTO;
import com.ayor.entity.admin.vo.AccountVO;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.AccountService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService  {

    private final RoleMapper roleMapper;

    private static final List<String> allowedRoles = List.of("OWNER");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.baseMapper.getAccountByName(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        String roleName = roleMapper.getRoleNameById(account.getRoleId());

        if (!allowedRoles.contains(roleName)) {
            throw new UsernameNotFoundException("用户权限不足");
        }

        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(roleName)
                .build();
    }

    @Override
    public List<AccountVO> getAccountsAsSelectOptions() {
        List<Account> accounts = this.lambdaQuery()
                .orderByDesc(Account::getCreateTime)
                .last("limit 10")
                .list();
        List<AccountVO> accountVos = new ArrayList<>();
        accounts.forEach(account -> {
            AccountVO accountVO = new AccountVO();
            accountVO.setAccountId(account.getAccountId());
            accountVO.setUsername(account.getUsername());
            accountVos.add(accountVO);
        });

        return accountVos;
    }

    @Override
    public AccountVO getAccountById(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        Account account = this.getById(accountId);
        AccountVO accountVO = new AccountVO();
        BeanUtils.copyProperties(account, accountVO);
        return accountVO;
    }

    @Override
    public PageEntity<AccountVO> getAccountsByRoleId(Integer pageNum, Integer pageSize, Integer roleId) {
        Page<Account> page = this.lambdaQuery()
                .like(roleId != null, Account::getRoleId, roleId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }

    @Override
    public PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize) {
        Page<Account> page = this.lambdaQuery().page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }

    @Override
    public PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize, Integer status) {
        Page<Account> page = this.lambdaQuery()
                .eq(status != null, Account::getStatus, status)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }

    @Override
    public String createAccount(AccountDTO accountDTO) {
        if (accountDTO == null || !StringUtils.hasText(accountDTO.getUsername())) {
            return "用户名不能为空";
        }
        Long count = this.lambdaQuery()
                .eq(Account::getUsername, accountDTO.getUsername())
                .count();
        if (count != null && count > 0) {
            return "用户名已存在";
        }
        Account account = new Account();
        BeanUtils.copyProperties(accountDTO, account);
        Date now = new Date();
        account.setCreateTime(now);
        account.setUpdateTime(now);
        return this.save(account) ? null : "创建用户失败";
    }

    @Override
    public String updateAccount(AccountDTO accountDTO) {
        if (accountDTO == null || accountDTO.getAccountId() == null) {
            return "用户不存在";
        }
        Account account = this.getById(accountDTO.getAccountId());
        if (account == null) {
            return "用户不存在";
        }
        BeanUtils.copyProperties(accountDTO, account);
        account.setUpdateTime(new Date());
        return this.updateById(account) ? null : "更新用户失败";
    }

    @Override
    public String deleteAccount(Integer accountId) {
        if (accountId == null) {
            return "用户不存在";
        }
        Account account = this.getById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        account.setDeleted(true);
        return this.updateById(account) ? null : "删除用户失败";
    }

    private List<AccountVO> toVoList(List<Account> accounts) {
        List<AccountVO> accountVos = new ArrayList<>();
        for (Account account : accounts) {
            AccountVO accountVO = new AccountVO();
            BeanUtils.copyProperties(account, accountVO);
            //TODO 待完善信息获取
            accountVos.add(accountVO);
        }
        return accountVos;
    }
}
