package com.ayor.service.impl;

import com.ayor.entity.vo.UserSearchVO;
import com.ayor.mapper.AccountMapper;
import com.ayor.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

    private final AccountMapper accountMapper;

    @Override
    public List<UserSearchVO> searchUsersForMention(String keyword, Integer currentUserId) {
        if (currentUserId == null) {
            return Collections.emptyList();
        }
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        return accountMapper.searchUsersForMention(keyword.trim(), currentUserId);
    }
}
