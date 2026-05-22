package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserSearchVO;
import com.ayor.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @Test
    void searchUsersShouldReturnEmptyPageWhenKeywordIsBlank() {
        UserSearchServiceImpl service = new UserSearchServiceImpl(accountMapper);

        PageEntity<UserSearchVO> result = service.searchUsers("   ", 1, 10);

        assertEquals(0L, result.getTotalSize());
        assertEquals(List.of(), result.getData());
        verify(accountMapper, never()).countSearchUsers("   ");
    }

    @Test
    void searchUsersShouldNormalizePagingAndTrimKeyword() {
        UserSearchServiceImpl service = new UserSearchServiceImpl(accountMapper);
        UserSearchVO user = new UserSearchVO(7, "ayor", "Ayor", "avatar.png");
        when(accountMapper.countSearchUsers("ay")).thenReturn(1L);
        when(accountMapper.searchUsers("ay", 50, 0)).thenReturn(List.of(user));

        PageEntity<UserSearchVO> result = service.searchUsers(" ay ", 0, 100);

        assertEquals(1L, result.getTotalSize());
        assertEquals(List.of(user), result.getData());
        verify(accountMapper).countSearchUsers("ay");
        verify(accountMapper).searchUsers("ay", 50, 0);
    }

    @Test
    void searchUsersShouldCalculateOffsetFromPageNum() {
        UserSearchServiceImpl service = new UserSearchServiceImpl(accountMapper);
        when(accountMapper.countSearchUsers("nick")).thenReturn(12L);
        when(accountMapper.searchUsers("nick", 5, 10)).thenReturn(List.of());

        PageEntity<UserSearchVO> result = service.searchUsers("nick", 3, 5);

        assertEquals(12L, result.getTotalSize());
        assertEquals(List.of(), result.getData());
        verify(accountMapper).searchUsers("nick", 5, 10);
    }
}
