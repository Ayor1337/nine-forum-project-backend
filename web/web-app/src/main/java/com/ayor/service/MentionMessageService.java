package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.MentionMessage;
import com.ayor.entity.vo.MentionMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MentionMessageService extends IService<MentionMessage> {

    void createThreadMentionMessages(String content, Integer fromAccountId, Integer threadId);

    void createPostMentionMessages(String content, Integer fromAccountId, Integer postId, Integer threadId);

    PageEntity<MentionMessageVO> listMentionMessages(Integer pageNum, Integer pageSize, Integer accountId);
}
