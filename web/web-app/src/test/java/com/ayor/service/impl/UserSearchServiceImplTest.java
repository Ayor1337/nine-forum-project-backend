package com.ayor.service.impl;

import com.ayor.entity.vo.UserSearchVO;
import com.ayor.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceImplTest {

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private UserSearchServiceImpl userSearchService;

    @Test
    void shouldReturnEmptyListWhenKeywordIsBlank() {
        assertTrue(userSearchService.searchUsersForMention("   ", 1).isEmpty());
    }

    @Test
    void shouldTrimKeywordBeforeDelegatingToMapper() {
        List<UserSearchVO> expected = List.of(new UserSearchVO(2, "alice", "Alice", "avatar"));
        when(accountMapper.searchUsersForMention("alice", 1)).thenReturn(expected);

        List<UserSearchVO> actual = userSearchService.searchUsersForMention(" alice ", 1);

        assertEquals(expected, actual);
        verify(accountMapper).searchUsersForMention("alice", 1);
    }
}
