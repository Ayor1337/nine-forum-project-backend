package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
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

    private static final int MAX_PAGE_SIZE = 50;

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

    @Override
    public PageEntity<UserSearchVO> searchUsers(String keyword, int pageNum, int pageSize) {
        if (keyword == null || keyword.isBlank()) {
            return new PageEntity<>(0L, Collections.emptyList());
        }
        String trimmedKeyword = keyword.trim();
        int normalizedPageNum = Math.max(pageNum, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        int offset = (normalizedPageNum - 1) * normalizedPageSize;

        Long total = accountMapper.countSearchUsers(trimmedKeyword);
        if (total == null || total == 0) {
            return new PageEntity<>(0L, Collections.emptyList());
        }
        List<UserSearchVO> users = accountMapper.searchUsers(trimmedKeyword, normalizedPageSize, offset);
        return new PageEntity<>(total, users);
    }
}
