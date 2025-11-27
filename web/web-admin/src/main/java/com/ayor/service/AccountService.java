package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.AccountDTO;
import com.ayor.entity.admin.vo.AccountVO;
import com.ayor.entity.pojo.Account;
import com.ayor.type.UserViolationType;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface AccountService extends UserDetailsService, IService<Account> {

    List<AccountVO> getAccountsAsSelectOptions();

    List<AccountVO> getAccountsAsSelectOptions(String query);

    AccountVO getAccountById(Integer accountId);

    PageEntity<AccountVO> getAccountsByRoleId(Integer pageNum, Integer pageSize, Integer roleId);

    PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize);

    PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize, Integer status);

    PageEntity<AccountVO> getAccounts(String query, Integer pageNum, Integer pageSize, Integer status);

    String violationProfile(Integer accountId, String type);

    String updateAccount(AccountDTO accountDTO);

    String deleteAccount(Integer accountId);
}
