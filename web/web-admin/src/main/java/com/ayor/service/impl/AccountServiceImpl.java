package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.AccountDTO;
import com.ayor.entity.admin.vo.AccountVO;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.entity.message.UserViolationMessageTemplate;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.AccountService;
import com.ayor.type.UserViolationType;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService  {

    private final RoleMapper roleMapper;

    private final RabbitTemplate rabbitTemplate;

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
    public List<AccountVO> getAccountsAsSelectOptions(String query) {
        if (!StringUtils.hasText(query)) {
            return getAccountsAsSelectOptions();
        }
        List<Account> accounts = this.lambdaQuery()
                .orderByDesc(Account::getCreateTime)
                .like(StringUtils.hasText(query), Account::getUsername, query)
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
        if (status == null) {
            return getAccounts(pageNum, pageSize);
        }
        Page<Account> page = this.lambdaQuery()
                .eq(Account::getStatus, status)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }

    @Override
    public PageEntity<AccountVO> getAccounts(String query, Integer pageNum, Integer pageSize, Integer status) {
        if (query == null) {
            return getAccounts(pageNum, pageSize, status);
        }
        Page<Account> page = this.lambdaQuery()
                .like(StringUtils.hasText(query), Account::getUsername, query)
                .eq(status != null, Account::getStatus, status)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }


    @Override
    @CacheEvict(value = "userInfo", key = "#accountId")
    public String violationProfile(Integer accountId, String type) {
        if (accountId == null || !existsUserById(accountId)) {
            return "用户不存在";
        }
        if (type == null) {
            return "用户违规类型不存在";
        }
        Account account = this.getById(accountId);
        UserViolationType userViolationType;
        UserViolationMessage<String> message;
        switch (type) {
            case "nickname" -> userViolationType = UserViolationType.NICKNAME_VIOLATION;
            case "banner" -> userViolationType = UserViolationType.BANNER_VIOLATION;
            case "avatar" -> userViolationType = UserViolationType.AVATAR_VIOLATION;
            default -> {
                return "用户违规类型不存在";
            }
        }
        switch (userViolationType) {
            case NICKNAME_VIOLATION -> {
                account.setNickname("改名"+ UUID.randomUUID().toString().substring(0,3));
                message = new UserViolationMessage<>(
                        UserViolationMessageTemplate.NICKNAME_VIOLATION,
                        "用户违规处理",
                        account.getAccountId(),
                        userViolationType
                );
            }
            case BANNER_VIOLATION -> {
                account.setBannerUrl("nineforum/banner/default.webp");
                message = new UserViolationMessage<>(
                        UserViolationMessageTemplate.BANNER_VIOLATION,
                        "用户违规处理",
                        account.getAccountId(),
                        userViolationType
                );
            }
            case AVATAR_VIOLATION -> {
                account.setAvatarUrl("nineforum/avatar/default.jpg");
                message = new UserViolationMessage<>(
                        UserViolationMessageTemplate.AVATAR_VIOLATION,
                        "用户违规处理",
                        account.getAccountId(),
                        userViolationType
                );
            }
            default -> {
                return "用户违规类型不存在";
            }
        }
        this.updateById(account);
        // 监听器还没做
        rabbitTemplate.convertAndSend("broadcast.direct", "broadcast", message);
        return null;
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

    private boolean existsUserById(Integer accountId) {
        return this.baseMapper.exists(Wrappers.<Account>lambdaQuery().eq(Account::getAccountId, accountId));
    }

}
