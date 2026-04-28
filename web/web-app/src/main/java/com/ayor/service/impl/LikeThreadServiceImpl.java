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
import com.ayor.service.PrivacyPolicyService;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
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

    private final TipTapUtils tipTapUtils;

    private final PrivacyPolicyService privacyPolicyService;
    /**
     * 为指定帖子记录一次点赞。
     */

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
    /**
     * 删除指定帖子上的点赞记录。
     */

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
    /**
     * 分页获取用户点赞过的帖子列表。
     *
     * @param viewerId 当前查看者用户ID
     * @param accountId 用户ID
     * @param currentPage 当前页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含用户点赞的帖子视图对象列表
     */
    @Override
    public PageEntity<ThreadVO> getLikesByAccountId(Integer viewerId,
                                                    Integer accountId,
                                                    Integer currentPage,
                                                    Integer pageSize) {
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return null;
        }
        if (!privacyPolicyService.canViewLikedThreads(viewerId, accountId)) {
            throw new AccessDeniedException("无权限查看点赞列表");
        }
        Page<LikeThread> page = new Page<>(currentPage, pageSize);
        Page<LikeThread> likePage = this.lambdaQuery().eq(LikeThread::getAccountId, accountId).page(page);
        List<LikeThread> likeThreads = likePage.getRecords();
        List<Integer> threadIds = new ArrayList<>();
        likeThreads.forEach(like -> threadIds.add(like.getThreadId()));
        List<Threadd> threads = threaddMapper.selectByIds(threadIds);
        List<ThreadVO> threadVOS = new ArrayList<>();
        for (Threadd thread : threads) {
            ThreadVO threadVO = new ThreadVO();
            BeanUtils.copyProperties(thread, threadVO);
            threadVO.setContent(tipTapUtils.filterNonImage(thread.getContent()));
            threadVOS.add(threadVO);
        }

        return new PageEntity<>(likePage.getTotal(), threadVOS);
    }
    /**
     * 获取指定帖子的点赞数。
     */

    @Override
    public Integer getLikeCountByThreadId(Integer threadId) {
        Integer likeCountByThreadId = this.baseMapper.getLikeCountByThreadId(threadId);
        return likeCountByThreadId == null ? 0 : likeCountByThreadId;
    }
    /**
     * 判断用户是否点赞了指定帖子。
     */

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
