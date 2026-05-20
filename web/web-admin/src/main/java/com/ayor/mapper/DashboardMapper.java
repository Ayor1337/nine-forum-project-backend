package com.ayor.mapper;

import com.ayor.entity.vo.ActivityVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DashboardMapper {

    @Select("SELECT COUNT(*) FROM account")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM account WHERE DATE(create_time) = CURRENT_DATE")
    Integer countTodayNewUsers();

    @Select("SELECT COUNT(*) FROM account WHERE DATE(create_time) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)")
    Integer countYesterdayNewUsers();

    @Select("""
            SELECT COUNT(DISTINCT account_id)
            FROM (
                SELECT account_id FROM thread WHERE DATE(create_time) = CURRENT_DATE
                UNION ALL
                SELECT account_id FROM post WHERE DATE(create_time) = CURRENT_DATE
                UNION ALL
                SELECT reporter_account_id AS account_id FROM report WHERE DATE(create_time) = CURRENT_DATE
            ) active_users
            WHERE account_id IS NOT NULL
            """)
    Integer countTodayActiveUsers();

    @Select("""
            SELECT IFNULL(active_hour, 0)
            FROM (
                SELECT active_hour, COUNT(*) AS active_count
                FROM (
                    SELECT HOUR(create_time) AS active_hour FROM thread WHERE DATE(create_time) = CURRENT_DATE
                    UNION ALL
                    SELECT HOUR(create_time) AS active_hour FROM post WHERE DATE(create_time) = CURRENT_DATE
                    UNION ALL
                    SELECT HOUR(create_time) AS active_hour FROM report WHERE DATE(create_time) = CURRENT_DATE
                ) active_events
                WHERE active_hour IS NOT NULL
                GROUP BY active_hour
                ORDER BY active_count DESC, active_hour ASC
                LIMIT 1
            ) peak
            """)
    Integer selectTodayActivePeakHour();

    @Select("SELECT COUNT(*) FROM thread")
    Long countTotalThreads();

    @Select("SELECT COUNT(*) FROM thread WHERE DATE(create_time) = CURRENT_DATE")
    Integer countTodayThreads();

    @Select("SELECT COUNT(*) FROM thread WHERE DATE(create_time) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)")
    Integer countYesterdayThreads();

    @Select("SELECT COUNT(*) FROM post WHERE DATE(create_time) = CURRENT_DATE")
    Integer countTodayPosts();

    @Select("SELECT COUNT(*) FROM post WHERE DATE(create_time) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)")
    Integer countYesterdayPosts();

    @Select("SELECT COUNT(*) FROM report WHERE DATE(create_time) = CURRENT_DATE")
    Integer countTodayReports();

    @Select("SELECT COUNT(*) FROM report WHERE DATE(create_time) = DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY)")
    Integer countYesterdayReports();

    @Select("SELECT COUNT(*) FROM report WHERE status IN ('PENDING', 'PROCESSING')")
    Integer countPendingReports();

    @Select("""
            SELECT COUNT(*)
            FROM report
            WHERE status IN ('PENDING', 'PROCESSING')
              AND report_type IN (
                  'ABUSE_HARASSMENT',
                  'PORNOGRAPHY',
                  'ILLEGAL_CONTENT',
                  'HARASSMENT',
                  'FRAUD',
                  'PRIVACY_VIOLATION'
              )
            """)
    Integer countHighPriorityReports();

    @Select("""
            SELECT COUNT(*)
            FROM report
            WHERE status IN ('PENDING', 'PROCESSING')
              AND create_time < DATE_SUB(NOW(), INTERVAL 24 HOUR)
            """)
    Integer countOverduePendingReports();

    @Select("""
            SELECT IFNULL(ROUND(AVG(TIMESTAMPDIFF(MINUTE, create_time, handled_at))), 0)
            FROM report
            WHERE handled_at IS NOT NULL
            """)
    Integer selectAvgReportResponseMinutes();

    @Select("""
            SELECT CONCAT(topic_title, '贡献 ', ROUND(topic_count * 100 / total_count), '%')
            FROM (
                SELECT IFNULL(topic.title, '未分区') AS topic_title,
                       COUNT(*) AS topic_count,
                       SUM(COUNT(*)) OVER () AS total_count
                FROM thread
                LEFT JOIN topic ON topic.topic_id = thread.topic_id
                WHERE DATE(thread.create_time) = CURRENT_DATE
                GROUP BY topic.topic_id, topic.title
                ORDER BY topic_count DESC
                LIMIT 1
            ) topic_summary
            """)
    String selectTopThreadTopicSummary();

    @Select("SELECT COUNT(*) FROM dashboard_activity")
    Long countDashboardActivities();

    @Select("""
            SELECT
                activity_id AS id,
                created_at AS createdAt,
                user_id AS userId,
                username,
                action,
                target,
                target_id AS targetId,
                type
            FROM dashboard_activity
            ORDER BY created_at DESC, activity_id DESC
            LIMIT #{pageSize} OFFSET #{offset}
            """)
    List<ActivityVO> selectDashboardActivities(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Insert("""
            INSERT INTO dashboard_activity (
                source_key,
                created_at,
                user_id,
                username,
                action,
                target,
                target_id,
                type
            )
            SELECT
                CONCAT('THREAD:', thread.thread_id) COLLATE utf8mb4_unicode_ci,
                thread.create_time,
                thread.account_id,
                COALESCE(
                    CONVERT(account.nickname USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    CONVERT(account.username USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    '系统' COLLATE utf8mb4_unicode_ci
                ),
                'POST_THREAD' COLLATE utf8mb4_unicode_ci,
                COALESCE(
                    CONVERT(thread.title USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    '未命名帖子' COLLATE utf8mb4_unicode_ci
                ),
                thread.thread_id,
                'thread' COLLATE utf8mb4_unicode_ci
            FROM thread
            LEFT JOIN account ON account.account_id = thread.account_id
            WHERE thread.create_time IS NOT NULL
            UNION ALL
            SELECT
                CONCAT('REPORT:', report.report_id) COLLATE utf8mb4_unicode_ci,
                report.create_time,
                report.reporter_account_id,
                COALESCE(
                    CONVERT(reporter.nickname USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    CONVERT(reporter.username USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    '系统' COLLATE utf8mb4_unicode_ci
                ),
                'SUBMIT_REPORT' COLLATE utf8mb4_unicode_ci,
                COALESCE(
                    CONVERT(report.target_summary_snapshot USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    CONVERT(report.reported_username_snapshot USING utf8mb4) COLLATE utf8mb4_unicode_ci,
                    '相关举报' COLLATE utf8mb4_unicode_ci
                ),
                report.report_id,
                'report' COLLATE utf8mb4_unicode_ci
            FROM report
            LEFT JOIN account reporter ON reporter.account_id = report.reporter_account_id
            WHERE report.create_time IS NOT NULL
            ON DUPLICATE KEY UPDATE
                created_at = VALUES(created_at),
                user_id = VALUES(user_id),
                username = VALUES(username),
                action = VALUES(action),
                target = VALUES(target),
                target_id = VALUES(target_id),
                type = VALUES(type)
            """)
    @Options(useGeneratedKeys = false)
    Integer refreshDashboardActivities();
}
