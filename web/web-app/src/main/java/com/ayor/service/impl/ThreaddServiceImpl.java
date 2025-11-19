package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.dto.TagUpdateDTO;
import com.ayor.entity.app.dto.ThreadDTO;
import com.ayor.entity.app.vo.AnnouncementVO;
import com.ayor.entity.app.vo.TagVO;
import com.ayor.entity.app.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.*;
import com.ayor.service.ThreaddService;
import com.ayor.util.QuillUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Transactional
@RequiredArgsConstructor
public class ThreaddServiceImpl extends ServiceImpl<ThreaddMapper, Threadd> implements ThreaddService {

    // TODO 防止 topic_id虚假然后删除了其他的thread (topic_id 与 threadId 不符)


    private final AccountMapper accountMapper;

    private final TopicMapper topicMapper;

    private final PostMapper postMapper;

    private final QuillUtils quillUtils;

    private final TagMapper tagMapper;

    @Override
    public List<ThreadVO> getThreadVOsByTopicId(Integer topicId) {
        if (topicId == null) {
            return null;
        }
        if (topicMapper.isTopicDelete(topicId)) {
            return null;
        }
        List<Threadd> threads = this.baseMapper.getThreadsByTopicId(topicId);
        return getThreadVOS(threads);
    }

    @Override
    public PageEntity<ThreadVO> getThreadVOsByTopicId(Integer topicId, Integer pageNum, Integer pageSize) {
        if (topicId == null) {
            return null;
        }
        if (topicMapper.isTopicDelete(topicId)) {
            return null;
        }
        Page<Threadd> threads = this.lambdaQuery()
                .eq(Threadd::getTopicId, topicId)
                .eq(Threadd::getIsDeleted, false)
                .page(Page.of(pageNum, pageSize));
        System.out.println(threads.getRecords().size());

        return new PageEntity<>(threads.getTotal(), getThreadVOS(threads.getRecords()));
    }

    @Override
    public String getThreadTitleById(Integer threadId) {
        if (threadId == null || !existsThreadById(threadId)) {
            return null;
        }
        Threadd threadd = this.baseMapper.selectById(threadId);
        return threadd.getTitle();
    }

    @Override
    public ThreadVO getThreadById(Integer threadId) {
        if (threadId == null || !existsThreadById(threadId)) {
            return null;
        }
        Threadd threadd = this.lambdaQuery().eq(Threadd::getThreadId, threadId).one();
        ThreadVO threadVO = new ThreadVO();
        TagVO tagVO = new TagVO();
        Account account = accountMapper.getAccountById(threadd.getAccountId());
        Tag tag = tagMapper.getTagById(threadd.getTagId());

        if (tag != null) {
            BeanUtils.copyProperties(tag, tagVO);
        }
        BeanUtils.copyProperties(threadd, threadVO);


        threadVO.setTag(tagVO);
        threadVO.setAccountName(account.getNickname());
        threadVO.setAvatarUrl(account.getAvatarUrl());
        threadVO.setAccountId(account.getAccountId());
        return threadVO;
    }

    @Override
    public PageEntity<ThreadVO> getThreadPagesByUserId(Integer accountId, Integer currentPage, Integer pageSize) {
        Page<Threadd> page = new Page<>(currentPage, pageSize);
        Page<Threadd> threads = this.lambdaQuery()
                .eq(Threadd::getAccountId, accountId)
                .eq(Threadd::getIsDeleted, false)
                .page(page);
        List<ThreadVO> threadVOS = getThreadVOS(threads.getRecords());
        Long totalPages = threads.getTotal();
        return new PageEntity<>(totalPages, threadVOS);
    }


    @NotNull
    private List<ThreadVO> getThreadVOS(List<Threadd> threads) {
        List<ThreadVO> threadVOList = new ArrayList<>();
        threads.forEach(threadd -> {
            if (!threadd.getIsDeleted()) {
                ThreadVO threadVO = new ThreadVO();
                TagVO tagVO = new TagVO();

                Account account = accountMapper.getAccountById(threadd.getAccountId());
                Tag tag = tagMapper.getTagById(threadd.getTagId());

                BeanUtils.copyProperties(threadd, threadVO);
                if (tag != null) {
                    BeanUtils.copyProperties(tag, tagVO);
                }

                threadVO.setTag(tagVO);
                threadVO.setAccountName(account.getNickname());
                threadVO.setContent(quillUtils.QuillDeltaFilterNonImage(threadd.getContent()));
                threadVO.setImageUrls(quillUtils.QuillDeltaFilterImage(threadd.getContent()));
                threadVO.setAvatarUrl(account.getAvatarUrl());
                threadVO.setAccountId(account.getAccountId());

                threadVOList.add(threadVO);
            }
        });
        return threadVOList;
    }

    @Override
    public String removeThreadById(Integer threadId, Integer accountId) {
        Threadd thread = this.getById(threadId);
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        if (!Objects.equals(account.getAccountId(), thread.getAccountId())) {
            return "权限不足";
        }
        if (thread.getIsDeleted()) {
            return "帖子已删除";
        }
        postMapper.removePostsByThreadId(threadId);
        return this.removeByIdLogical(threadId) ? null : "删除失败";
    }

    public String permRemoveThreadById(Integer threadId) {
        Threadd thread = this.getById(threadId);
        if (thread == null) {
            return "帖子不存在";
        }
        if (thread.getIsDeleted()) {
            return "帖子已删除";
        }
        postMapper.removePostsByThreadId(threadId);
        return this.removeByIdLogical(threadId) ? null : "删除失败";
    }

    @Override
    public String setAnnouncementByThreadId(Integer threadId, Integer topicId) {
        Threadd thread = this.lambdaQuery()
                .eq(Threadd::getThreadId, threadId)
                .eq(Threadd::getTopicId, topicId)
                .one();
        if (thread == null || thread.getIsDeleted()) {
            return "帖子不存在";
        }
        if (thread.getIsAnnouncement()) {
            return "该帖子已经是公告";
        }
        thread.setIsAnnouncement(true);
        return this.updateById(thread) ? null : "修改失败";
    }

    @Override
    public String removeAnnouncementByThreadId(Integer threadId, Integer topicId) {
        Threadd thread = this.lambdaQuery()
                .eq(Threadd::getThreadId, threadId)
                .eq(Threadd::getTopicId, topicId)
                .one();
        if (thread == null || thread.getIsDeleted()) {
            return "帖子不存在";
        }
        if (!thread.getIsAnnouncement()) {
            return "该帖子不是公告";
        }
        thread.setIsAnnouncement(false);
        return this.updateById(thread) ? null : "修改失败";
    }

    @Override
    public List<AnnouncementVO> getAnnouncementThreads(Integer topicId) {
        if (topicId == null) {
            return null;
        }
        List<Threadd> announcements = this.baseMapper.getAnnouncementsByTopicId(topicId);
        List<AnnouncementVO> announcementVOList = new ArrayList<>();
        announcements.forEach(announcement -> {
            if (!announcement.getIsDeleted()) {
                AnnouncementVO announcementVO = new AnnouncementVO();
                BeanUtils.copyProperties(announcement, announcementVO);
                announcementVOList.add(announcementVO);
            }
        });
        return announcementVOList;
    }

    @Override
    public String insertThread(ThreadDTO threadDTO, Integer accountId) {
        if (accountId == null) {
            return "用户不存在";
        }
        Threadd threadd = new Threadd();
        BeanUtils.copyProperties(threadDTO, threadd);
        threadd.setContent(quillUtils.QuillDeltaConvertBase64ToURL(threadDTO.getContent(), "threads/" + threadd.getTopicId() + "/"));
        threadd.setAccountId(accountId);
        threadd.setCreateTime(new Date());

        return this.save(threadd) ? null : "添加失败";
    }

    @Override
    public String updateThreadTag(TagUpdateDTO tagUpdateDTO) {
        if (tagUpdateDTO == null) {
            return "参数错误";
        }
        Threadd threadd = this.lambdaQuery()
                .eq(Threadd::getThreadId, tagUpdateDTO.getThreadId())
                .eq(Threadd::getTopicId, tagUpdateDTO.getTopicId())
                .one();
        if (threadd == null) {
            return "帖子不存在";
        }
        Tag tag = tagMapper.getTagById(tagUpdateDTO.getTagId());
        if (tag == null) {
            return "标签不存在";
        }
        threadd.setTagId(tagUpdateDTO.getTagId());

        return this.updateById(threadd) ? null : "修改失败";
    }

    @Override
    public String removeThreadTag(Integer threadId, Integer topicId) {
        if (!existsThreadById(threadId)) {
            return "帖子不存在";
        }
        return this.baseMapper.removeThreadTag(threadId, topicId) ? null : "修改失败";
    }

    @Override
    public void updateThreadStat() {
        this.baseMapper.updateThreadPostCount();
        this.baseMapper.updateLikeCount();
    }

    @Override
    public String updateViewCount(Integer threadId) {
        Threadd threadd = this.lambdaQuery().eq(Threadd::getThreadId, threadId).one();
        if (threadd == null) {
            return "帖子不存在";
        }
        Lock lock = new ReentrantLock();
        lock.lock();
        threadd.setViewCount(threadd.getViewCount() + 1);
        this.updateById(threadd);
        lock.unlock();
        return null;
    }

    private boolean existsThreadById(Integer threadId) {
        Threadd threadd = this.lambdaQuery().eq(Threadd::getThreadId, threadId).one();
        return threadd != null && !threadd.getIsDeleted();
    }

    private boolean removeByIdLogical(Serializable Id) {
        Threadd threadd = this.getById(Id);
        if (threadd == null) {
            return false;
        }
        threadd.setIsDeleted(true);
        return this.updateById(threadd);
    }



}
