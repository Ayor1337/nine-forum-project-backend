package com.ayor.controller;

import com.ayor.entity.vo.UserSearchVO;
import com.ayor.result.Result;
import com.ayor.service.UserSearchService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    private final SecurityUtils securityUtils;

    @GetMapping("/search")
    public Result<List<UserSearchVO>> searchUsers(@RequestParam("keyword") String keyword) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> userSearchService.searchUsersForMention(keyword, userId), "搜索用户失败");
    }
}
