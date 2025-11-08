package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.admin.dto.ThreadDTO;
import com.ayor.entity.admin.vo.ThreadTableVO;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.mapper.TopicMapper;
import com.ayor.service.ThreaddService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ThreaddServiceImpl extends ServiceImpl<ThreaddMapper, Threadd> implements ThreaddService {

    private final AccountMapper accountMapper;

    private final TopicMapper topicMapper;

    @Override
    public PageEntity<ThreadTableVO> getThreads(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            return null;
        }
        Page<Threadd> page = this.lambdaQuery().page(new Page<>(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public String createThread(ThreadDTO threadDTO) {
        if (threadDTO == null || !StringUtils.hasText(threadDTO.getTitle())) {
            return "帖子标题不能为空";
        }
        Threadd threadd = new Threadd();
        BeanUtils.copyProperties(threadDTO, threadd);
        Date now = new Date();
        if (threadd.getCreateTime() == null) {
            threadd.setCreateTime(now);
        }
        threadd.setUpdateTime(now);
        threadd.setIsDeleted(false);
        return this.save(threadd) ? null : "创建帖子失败";
    }

    @Override
    public String updateThread(ThreadDTO threadDTO) {
        if (threadDTO == null || threadDTO.getThreadId() == null) {
            return "帖子不存在";
        }
        Threadd threadd = this.getById(threadDTO.getThreadId());
        if (threadd == null) {
            return "帖子不存在";
        }
        Date originalCreateTime = threadd.getCreateTime();
        BeanUtils.copyProperties(threadDTO, threadd);
        if (threadDTO.getCreateTime() == null) {
            threadd.setCreateTime(originalCreateTime);
        }
        threadd.setUpdateTime(new Date());
        return this.updateById(threadd) ? null : "更新帖子失败";
    }

    @Override
    public String deleteThread(Integer threadId) {
        if (threadId == null) {
            return "帖子不存在";
        }
        Threadd threadd = this.getById(threadId);
        if (threadd == null) {
            return "帖子不存在";
        }
        threadd.setIsDeleted(true);
        return this.updateById(threadd) ? null : "删除帖子失败";
    }

    private List<ThreadTableVO> toVOList (List<Threadd> threads) {
        List<ThreadTableVO> threadTableVOList = new ArrayList<>();
        threads.forEach(threadd -> {
            ThreadTableVO threadTableVO = new ThreadTableVO();
            BeanUtils.copyProperties(threadd, threadTableVO);
            //TODO 添加逻辑待完善
            threadTableVO.setAccountName(accountMapper.getUsernameById(threadd.getAccountId()));
            threadTableVO.setTopicName(topicMapper.getTopicNameById(threadd.getTopicId()));
            threadTableVOList.add(threadTableVO);
        });
        return threadTableVOList;
    }

}
