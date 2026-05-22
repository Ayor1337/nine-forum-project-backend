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
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    private final UserSearchService userSearchService;

    private final SecurityUtils securityUtils;
    /**
     * 在 Elasticsearch 中搜索帖子并返回分页结果。
     *
     * @param query 搜索关键字
     * @param onlyThreadTopic 是否只搜索帖子本体
     * @param topicId 主题过滤条件
     * @param enableHistory 是否记录搜索历史
     * @param startTime 起始时间戳
     * @param endTime 结束时间戳
     * @param order 排序方式
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 帖子搜索结果
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
     * 搜索用户并返回分页结果。
     *
     * @param query 搜索关键字
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 用户搜索结果
     */

    @GetMapping("/users")
    public Result<PageEntity<UserSearchVO>> searchUser(@RequestParam(name = "query") String query,
                                                       @RequestParam(name = "page_num", defaultValue = "1") int pageNum,
                                                       @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        return Result.dataMessageHandler(() -> userSearchService.searchUsers(query, pageNum, pageSize), "搜索用户失败");
    }
    /**
     * 获取当前用户的全部搜索历史。
     *
     * @return 搜索历史集合
     */

    @GetMapping("/history")
    public Result<Set<String>> getSearchHistory() {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }
    /**
     * 获取当前用户的搜索历史查询入口。
     *
     * @param query 预留的关键字参数，当前实现不参与过滤
     * @return 搜索历史集合
     */

    @GetMapping("/history/query")
    public Result<Set<String>> getSearchQueryHistory(@RequestParam(name = "query", required = false) String query) {
        Integer userId = securityUtils.getSecurityUserId();
        return Result.dataMessageHandler(() -> searchService.getSearchHistory(userId), "获取搜索历史失败");
    }
    /**
     * 删除当前用户的搜索历史。
     *
     * @param query 可选关键字；不传则清空全部历史
     * @return 删除结果
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
     * 获取热门搜索词。
     *
     * @param size 返回数量
     * @param duration 统计天数
     * @return 热门关键词列表
     */

    @GetMapping("/hot-keywords")
    public Result<List<HotKeywordVO>> getHotSearch(@RequestParam(name = "size", defaultValue = "10") int size,
                                                   @RequestParam(name = "duration", defaultValue = "7") int duration) {
        return Result.dataMessageHandler(() -> searchService.getHotKeywords(size, Duration.ofDays(duration)), "获取热门搜索失败");
    }
}
