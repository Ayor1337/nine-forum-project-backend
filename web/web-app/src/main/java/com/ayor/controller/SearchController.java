package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.document.ThreadDoc;
import com.ayor.entity.app.vo.HotKeywordVO;
import com.ayor.result.Result;
import com.ayor.service.SearchService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    private final SecurityUtils securityUtils;
    /**
     * searchThread 方法。
     */

    @GetMapping("/threads")
    public Result<PageEntity<ThreadDoc>> searchThread(@RequestParam(name = "query") String query,
                                     @RequestParam(name = "only_thread_topic", defaultValue = "false") boolean onlyThreadTopic,
                                     @RequestParam(name = "topic_id", required = false) Integer topicId,
                                     @RequestParam(name = "enable_history", defaultValue = "true") boolean enableHistory,
                                     @RequestParam(name = "start_time", required = false) Long startTime,
                                     @RequestParam(name = "end_time", required = false) Long endTime,
                                     @RequestParam(name = "order", defaultValue = "rel") String order,
                                     @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                     @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.searchThreads(
                query, userId, topicId, enableHistory, onlyThreadTopic, startTime, endTime, order, pageNum, pageSize), "搜索失败");
    }
    /**
     * searchUser 方法。
     */

    @GetMapping("/users")
    public Result<Void> searchUser(@RequestParam(name = "query") String query,
                                   @RequestParam(name = "only_thread_topic", defaultValue = "false") boolean onlyThreadTopic,
                                   @RequestParam(name = "topic_id", required = false) Integer topicId,
                                   @RequestParam(name = "enable_history", defaultValue = "true") boolean enableHistory,
                                   @RequestParam(name = "duration", defaultValue = "7") int duration,
                                   @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                   @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        // TODO
        return Result.ok();
    }
    /**
     * getSearchHistory 方法。
     */

    @GetMapping("/history")
    public Result<Set<String>> getSearchHistory() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }
    /**
     * getSearchQueryHistory 方法。
     */

    @GetMapping("/history/query")
    public Result<Set<String>> getSearchQueryHistory(@RequestParam(name = "query", required = false) String query) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }
    /**
     * deleteSearchHistory 方法。
     */

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
    /**
     * getHotSearch 方法。
     */

    @GetMapping("/hot-keywords")
    public Result<List<HotKeywordVO>> getHotSearch(@RequestParam(name = "size", defaultValue = "10") int size,
                                                   @RequestParam(name = "duration", defaultValue = "7") int duration) {
        return Result.dataMessageHandler(() -> searchService.getHotKeywords(size, Duration.ofDays(duration)), "获取热门搜索失败");
    }
}
