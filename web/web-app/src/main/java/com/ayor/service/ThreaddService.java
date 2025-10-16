package com.ayor.service;

import com.ayor.entity.app.dto.TagUpdateDTO;
import com.ayor.entity.app.dto.ThreadDTO;
import com.ayor.entity.app.vo.AnnouncementVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Threadd;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ThreaddService extends IService<Threadd> {
    List<ThreadVO> getThreadVOsByTopicId(Integer topicId);

    String getThreadTitleById(Integer threadId);

    ThreadVO getThreadById(Integer threadId);

    List<ThreadVO> getThreadsByUserId(Integer userId);

    String removeThreadById(Integer threadId, String username);

    String permRemoveThreadById(Integer threadId);

    String setAnnouncementByThreadId(Integer threadId, Integer topicId);

    String removeAnnouncementByThreadId(Integer threadId, Integer topicId);

    List<AnnouncementVO> getAnnouncementThreads(Integer topicId);

    String insertThread(ThreadDTO threadDTO, String username);

    String updateThreadTag(TagUpdateDTO tagUpdateDTO);

    String removeThreadTag(Integer threadId, Integer topicId);

    void updateThreadStat();

    String updateViewCount(Integer threadId);
}
