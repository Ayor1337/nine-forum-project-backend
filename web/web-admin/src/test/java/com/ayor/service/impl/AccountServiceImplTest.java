package com.ayor.service.impl;

import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.security.AdminRoleRequiredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private AccountMapper accountMapper;

    @Test
    void rejectsNonOwnerAccountsWithRoleSpecificAuthenticationException() {
        AccountServiceImpl service = service();
        Account account = account("member", 3);
        when(accountMapper.getAccountByName("member")).thenReturn(account);
        when(roleMapper.getRoleNameById(3)).thenReturn("USER");

        assertThatThrownBy(() -> service.loadUserByUsername("member"))
                .isInstanceOf(AdminRoleRequiredException.class)
                .hasMessage("用户权限不足");
    }

    @Test
    void loadsOwnerAccountsWithOwnerAuthority() {
        AccountServiceImpl service = service();
        Account account = account("owner", 1);
        when(accountMapper.getAccountByName("owner")).thenReturn(account);
        when(roleMapper.getRoleNameById(1)).thenReturn("OWNER");

        UserDetails details = service.loadUserByUsername("owner");

        assertThat(details.getUsername()).isEqualTo("owner");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_OWNER");
    }

    private AccountServiceImpl service() {
        AccountServiceImpl service = new AccountServiceImpl(roleMapper, rabbitTemplate);
        ReflectionTestUtils.setField(service, "baseMapper", accountMapper);
        return service;
    }

    private Account account(String username, int roleId) {
        Account account = new Account();
        account.setUsername(username);
        account.setPassword("encoded-password");
        account.setRoleId(roleId);
        return account;
    }
}
