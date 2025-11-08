package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.AccountDTO;
import com.ayor.entity.admin.vo.AccountVO;
import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface AccountService extends UserDetailsService, IService<Account> {

    List<AccountVO> getAccountsAsSelectOptions();

    AccountVO getAccountById(Integer accountId);

    PageEntity<AccountVO> getAccountsByRoleId(Integer pageNum, Integer pageSize, Integer roleId);

    PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize);

    PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize, Integer status);

    String createAccount(AccountDTO accountDTO);

    String updateAccount(AccountDTO accountDTO);

    String deleteAccount(Integer accountId);
}
