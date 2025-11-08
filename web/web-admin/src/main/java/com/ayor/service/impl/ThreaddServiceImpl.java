package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
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

import java.util.ArrayList;
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
