package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.LikeThread;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.LikeThreadMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.LikeThreadService;
import com.ayor.util.QuillUtils;
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
public class LikeThreadServiceImpl extends ServiceImpl<LikeThreadMapper, LikeThread> implements LikeThreadService {

    private final AccountMapper accountMapper;

    private final ThreaddMapper threaddMapper;

    private final QuillUtils quillUtils;

    @Override
    public String insertLikeThreadId(Integer accountId, Integer threadId) {
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        if (isLikedByAccountId(accountId, threadId)) {
            return "不能重复点赞";
        }
        LikeThread likeThread = new LikeThread();
        likeThread.setThreadId(threadId);
        likeThread.setAccountId(account.getAccountId());

        return this.save(likeThread) ? null : "点赞失败";
    }

    @Override
    public String removeLikeThreadId(Integer accountId, Integer threadId) {
        if (accountId == null) {
            return "取消点赞失败";
        }
        LikeThread likeThread = this.lambdaQuery().eq(LikeThread::getAccountId, accountId)
                .eq(LikeThread::getThreadId, threadId)
                .one();
        if (likeThread == null) {
            return "不能取消未点赞的内容";
        }
        return this.removeById(likeThread) ? null : "取消点赞失败";
    }

    @Override
    public PageEntity<ThreadVO> getLikesByAccountId(Integer accountId,
                                                    Integer currentPage,
                                                    Integer pageSize) {
        Page<LikeThread> page = new Page<>(currentPage, pageSize);
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        Page<LikeThread> likePage = this.lambdaQuery().eq(LikeThread::getAccountId, accountId).page(page);
        List<LikeThread> likeThreads = likePage.getRecords();
        List<Integer> threadIds = new ArrayList<>();
        likeThreads.forEach(like -> threadIds.add(like.getThreadId()));
        List<Threadd> threads = threaddMapper.selectByIds(threadIds);
        List<ThreadVO> threadVOS = new ArrayList<>();
        for (Threadd thread : threads) {
            ThreadVO threadVO = new ThreadVO();
            BeanUtils.copyProperties(thread, threadVO);
            threadVO.setContent(quillUtils.QuillDeltaFilterNonImage(thread.getContent()));
            threadVOS.add(threadVO);
        }

        return new PageEntity<>(likePage.getTotal(), threadVOS);
    }

    @Override
    public Integer getLikeCountByThreadId(Integer threadId) {
        Integer likeCountByThreadId = this.baseMapper.getLikeCountByThreadId(threadId);
        return likeCountByThreadId == null ? 0 : likeCountByThreadId;
    }

    @Override
    public Boolean isLikedByAccountId(Integer accountId, Integer threadId) {
        if(accountId == null) {
            return false;
        }
        return this.lambdaQuery()
                .eq(LikeThread::getAccountId, accountId)
                .eq(LikeThread::getThreadId, threadId).count() > 0;
    }

}
