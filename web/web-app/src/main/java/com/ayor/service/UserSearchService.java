package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserSearchVO;

import java.util.List;

public interface UserSearchService {

    List<UserSearchVO> searchUsersForMention(String keyword, Integer currentUserId);

    PageEntity<UserSearchVO> searchUsers(String keyword, int pageNum, int pageSize);
}
