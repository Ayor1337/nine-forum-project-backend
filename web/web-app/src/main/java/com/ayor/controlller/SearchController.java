package com.ayor.controlller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.documennt.ThreadDoc;
import com.ayor.result.Result;
import com.ayor.service.SearchService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    private final SecurityUtils securityUtils;

    @GetMapping("/query")
    public Result<PageEntity<ThreadDoc>> search(@RequestParam(name = "query") String query,
                                     @RequestParam(name = "pageNum", defaultValue = "1") int pageNum,
                                     @RequestParam(name = "pageSize", defaultValue = "10") int pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.searchThreads(query, userId, pageNum, pageSize), "搜索失败");
    }

    @GetMapping("/history")
    public Result<Set<String>> getSearchHistory() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }

    @GetMapping("/query/history")
    public Result<Set<String>> getSearchQueryHistory(@RequestParam(name = "query", required = false) String query) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }

    @DeleteMapping("/history")
    public Result<Void> deleteSearchHistory(@RequestParam(name = "query", required = false) String query) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> {
            if (query == null) {
                return searchService.removeSearchHistory(userId);
            }
            return searchService.removeSearchHistory(query, userId);
        });
    }
}
