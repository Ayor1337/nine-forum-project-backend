package com.ayor.service.impl;

import com.ayor.entity.app.vo.ThreadPageVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Like;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.LikeMapper;
import com.ayor.mapper.ThreaddMapper;
import com.ayor.service.LikeService;
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
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    private final AccountMapper accountMapper;

    private final ThreaddMapper threaddMapper;

    private final QuillUtils quillUtils;

    @Override
    public String insertLikeThreadId(String username, Integer threadId) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            return "用户不存在";
        }
        if (isLikedByUsername(username, threadId)) {
            return "不能重复点赞";
        }
        Like like = new Like();
        like.setThreadId(threadId);
        like.setAccountId(account.getAccountId());

        return this.save(like) ? null : "点赞失败";
    }

    @Override
    public String removeLikeThreadId(String username, Integer threadId) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            return "取消点赞失败";
        }
        Like like = this.lambdaQuery().eq(Like::getAccountId, account.getAccountId())
                .eq(Like::getThreadId, threadId)
                .one();
        if (like == null) {
            return "不能取消未点赞的内容";
        }
        return this.removeById(like) ? null : "取消点赞失败";
    }

    @Override
    public ThreadPageVO getLikesByAccountId(Integer accountId, Integer currentPage, Integer pageSize) {
        Page<Like> page = new Page<>(currentPage, pageSize);
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        Page<Like> likePage = this.lambdaQuery().eq(Like::getAccountId, accountId).page(page);
        List<Like> likes = likePage.getRecords();
        List<Integer> threadIds = new ArrayList<>();
        likes.forEach(like -> threadIds.add(like.getThreadId()));
        List<Threadd> threads = threaddMapper.getThreadsByIds(threadIds);
        List<ThreadVO> threadVOS = new ArrayList<>();
        for (Threadd thread : threads) {
            ThreadVO threadVO = new ThreadVO();
            BeanUtils.copyProperties(thread, threadVO);
            threadVO.setContent(quillUtils.QuillDeltaFilterNonImage(thread.getContent()));
            threadVOS.add(threadVO);
        }
        ThreadPageVO threadPageVO = new ThreadPageVO((int) likePage.getTotal(), threadVOS);

        return threadPageVO;
    }

    @Override
    public Integer getLikeCountByThreadId(Integer threadId) {
        Integer likeCountByThreadId = this.baseMapper.getLikeCountByThreadId(threadId);
        return likeCountByThreadId == null ? 0 : likeCountByThreadId;
    }

    @Override
    public Boolean isLikedByUsername(String username, Integer threadId) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            return false;
        }
        return this.lambdaQuery()
                .eq(Like::getAccountId, account.getAccountId())
                .eq(Like::getThreadId, threadId).count() > 0;
    }

}
