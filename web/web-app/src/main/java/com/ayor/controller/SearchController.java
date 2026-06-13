package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.entity.vo.HotKeywordVO;
import com.ayor.entity.vo.UserSearchVO;
import com.ayor.result.Result;
import com.ayor.service.SearchService;
import com.ayor.service.UserSearchService;
import com.ayor.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "搜索")
public class SearchController {

    private final SearchService searchService;

    private final UserSearchService userSearchService;

    private final SecurityUtils securityUtils;
    /**
     * 在 Elasticsearch 中搜索帖子并返回分页结果。
     */
    @Operation(summary = "搜索帖子")
    @GetMapping("/threads")
    public Result<PageEntity<ThreadDoc>> searchThread(@Parameter(description = "搜索关键字") @RequestParam(name = "query") String query,
                                     @Parameter(description = "是否只搜索帖子本体") @RequestParam(name = "only_thread_topic", defaultValue = "false") boolean onlyThreadTopic,
                                     @Parameter(description = "主题 ID") @RequestParam(name = "topic_id", required = false) Integer topicId,
                                     @Parameter(description = "是否记录搜索历史") @RequestParam(name = "enable_history", defaultValue = "true") boolean enableHistory,
                                     @Parameter(description = "起始时间戳") @RequestParam(name = "start_time", required = false) Long startTime,
                                     @Parameter(description = "结束时间戳") @RequestParam(name = "end_time", required = false) Long endTime,
                                     @Parameter(description = "排序方式") @RequestParam(name = "order", defaultValue = "rel") String order,
                                     @Parameter(description = "页码") @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                     @Parameter(description = "每页数量") @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.searchThreads(
                query, userId, topicId, enableHistory, onlyThreadTopic, startTime, endTime, order, pageNum, pageSize), "搜索失败");
    }
    /**
     * 搜索用户并返回分页结果。
     */
    @Operation(summary = "搜索用户")
    @GetMapping("/users")
    public Result<PageEntity<UserSearchVO>> searchUser(@Parameter(description = "搜索关键字") @RequestParam(name = "query") String query,
                                                       @Parameter(description = "页码") @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                                       @Parameter(description = "每页数量") @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        return Result.dataMessageHandler(() -> userSearchService.searchUsers(query, pageNum, pageSize), "搜索用户失败");
    }
    /**
     * 获取当前用户的全部搜索历史。
     */
    @Operation(summary = "获取当前用户的全部搜索历史")
    @GetMapping("/history")
    public Result<Set<String>> getSearchHistory() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }
    /**
     * 获取当前用户的搜索历史查询入口。
     */
    @Operation(summary = "获取当前用户的搜索历史")
    @GetMapping("/history/query")
    public Result<Set<String>> getSearchQueryHistory(@Parameter(description = "预留关键字") @RequestParam(name = "query", required = false) String query) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }
    /**
     * 删除当前用户的搜索历史。
     */
    @Operation(summary = "删除当前用户的搜索历史")
    @DeleteMapping("/history")
    public Result<Void> deleteSearchHistory(@Parameter(description = "可选关键字") @RequestParam(name = "query", required = false) String query) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.messageHandler(() -> {
            if (query == null) {
                return searchService.removeSearchHistory(userId);
            }
            return searchService.removeSearchHistory(query, userId);
        });
    }
    /**
     * 获取热门搜索词。
     */
    @Operation(summary = "获取热门搜索词")
    @GetMapping("/hot-keywords")
    public Result<List<HotKeywordVO>> getHotSearch(@Parameter(description = "返回数量") @RequestParam(name = "size", defaultValue = "10") int size,
                                                   @Parameter(description = "统计天数") @RequestParam(name = "duration", defaultValue = "7") int duration) {
        return Result.dataMessageHandler(() -> searchService.getHotKeywords(size, Duration.ofDays(duration)), "获取热门搜索失败");
    }
}
