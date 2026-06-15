package com.ayor.mapper;

import com.ayor.entity.pojo.Announcement;
import com.ayor.entity.vo.AnnouncementVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Select("""
            SELECT
                announcements.announcement_id AS announcementId,
                announcements.thread_id AS threadId,
                thread.topic_id AS topicId,
                thread.title,
                announcements.is_global AS isGlobal,
                announcements.create_time AS createTime
            FROM announcements
            INNER JOIN thread ON thread.thread_id = announcements.thread_id
            WHERE announcements.is_global = 0
              AND thread.topic_id = #{topicId}
              AND thread.is_deleted = 0
            ORDER BY announcements.create_time DESC
            """)
    List<AnnouncementVO> getTopicAnnouncements(Integer topicId);

    @Select("""
            SELECT
                announcements.announcement_id AS announcementId,
                announcements.thread_id AS threadId,
                thread.topic_id AS topicId,
                thread.title,
                announcements.is_global AS isGlobal,
                announcements.create_time AS createTime
            FROM announcements
            INNER JOIN thread ON thread.thread_id = announcements.thread_id
            WHERE announcements.is_global = 1
              AND thread.is_deleted = 0
            ORDER BY announcements.create_time DESC
            """)
    List<AnnouncementVO> getGlobalAnnouncements();
}
