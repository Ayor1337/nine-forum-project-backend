package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.UserSearchVO;

import java.util.List;

/**
 * 用户搜索服务接口
 *
 * 提供用户搜索能力，支持 @提及搜索和通用搜索两种场景。
 *
 * 主要功能:
 * - @提及用户搜索
 * - 通用用户搜索
 *
 * @see UserSearchVO 用户搜索结果视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface UserSearchService {

    List<UserSearchVO> searchUsersForMention(String keyword, Integer currentUserId);

    PageEntity<UserSearchVO> searchUsers(String keyword, int pageNum, int pageSize);
}
