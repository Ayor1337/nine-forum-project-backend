package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.vo.AccountVO;
import com.ayor.entity.message.UserViolationMessage;
import com.ayor.entity.message.UserViolationMessageTemplate;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.AccountService;
import com.ayor.type.AccountStatus;
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

    /**
     * 按用户名加载后台登录所需的认证信息，并校验该账号是否具备进入管理端的角色。
     * <p>账号不存在或角色不在允许列表中时，直接抛出 {@link UsernameNotFoundException}。</p>
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.baseMapper.getAccountByName(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        System.out.println(username);
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

    /**
     * 获取后台用户下拉选项，默认返回最近创建的 10 个账号。
     */
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

    /**
     * 按关键字筛选后台用户下拉选项，关键字为空时退回默认列表。
     */
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

    /**
     * 根据账号 ID 读取单个用户的展示信息。
     */
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

    /**
     * 按角色分页查询用户，供角色关联用户管理页面使用。
     */
    @Override
    public PageEntity<AccountVO> getAccountsByRoleId(Integer pageNum, Integer pageSize, Integer roleId) {
        Page<Account> page = this.lambdaQuery()
                .like(roleId != null, Account::getRoleId, roleId)
                .page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }

    /**
     * 分页查询全部用户，不附加额外过滤条件。
     */
    @Override
    public PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize) {
        Page<Account> page = this.lambdaQuery().page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVoList(page.getRecords()));
    }

    /**
     * 按状态分页查询用户；状态为空时退回到全部用户查询。
     */
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

    /**
     * 按关键字和状态联合分页查询用户。
     * <p>关键字为空时仅按状态过滤；状态为空时仅按关键字过滤。</p>
     */
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

    /**
     * 对指定用户执行违规处理，并同步发送广播消息给前台或其他消费者。
     * <p>支持昵称、头像、横幅三类处理；处理前会先刷新用户缓存。</p>
     */
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
    @CacheEvict(value = "userInfo", key = "#accountId", condition = "#accountId != null")
    public String updateAccountStatus(Integer accountId, AccountStatus status, String reason) {
        if (accountId == null) {
            return "用户不存在";
        }
        if (status == null || status == AccountStatus.ACTIVE) {
            return "账号状态不合法";
        }
        Account account = this.getById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        account.setStatus(status.getCode());
        account.setUpdateTime(new Date());
        if (!this.updateById(account)) {
            return "更新用户状态失败";
        }
        sendStatusNotification(accountId, status, reason);
        return null;
    }

    @Override
    @CacheEvict(value = "userInfo", key = "#accountId", condition = "#accountId != null")
    public String restoreAccount(Integer accountId) {
        if (accountId == null) {
            return "用户不存在";
        }
        Account account = this.getById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        if (AccountStatus.fromCode(account.getStatus()) == AccountStatus.ACTIVE) {
            return null;
        }
        account.setStatus(AccountStatus.ACTIVE.getCode());
        account.setUpdateTime(new Date());
        if (!this.updateById(account)) {
            return "恢复用户状态失败";
        }
        rabbitTemplate.convertAndSend("broadcast.direct", "broadcast",
                new UserSystemMessage<>("您的账号状态已恢复正常。", "账号状态恢复", accountId));
        return null;
    }

    /**
     * 按管理端提交的表单更新用户资料。
     */
    @Override
    @CacheEvict(value = "userInfo", key = "#accountDTO.accountId", condition = "#accountDTO != null && #accountDTO.accountId != null")
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

    /**
     * 逻辑删除用户，将删除标记置为 true。
     */
    @Override
    @CacheEvict(value = "userInfo", key = "#accountId", condition = "#accountId != null")
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

    /**
     * 将用户实体转换成管理端列表对象。
     */
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

    /**
     * 判断指定账号是否存在，供违规处理等流程复用。
     */
    private boolean existsUserById(Integer accountId) {
        return this.baseMapper.exists(Wrappers.<Account>lambdaQuery().eq(Account::getAccountId, accountId));
    }

    private void sendStatusNotification(Integer accountId, AccountStatus status, String reason) {
        String action = status == AccountStatus.MUTED ? "禁言" : "封禁";
        StringBuilder content = new StringBuilder("您的账号已被").append(action).append("。");
        if (StringUtils.hasText(reason)) {
            content.append("处理备注：").append(reason.trim()).append("。");
        }
        rabbitTemplate.convertAndSend("broadcast.direct", "broadcast",
                new UserSystemMessage<>(content.toString(), "账号状态变更", accountId));
    }

}
