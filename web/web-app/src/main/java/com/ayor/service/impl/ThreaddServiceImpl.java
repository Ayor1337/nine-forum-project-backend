package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.vo.AnnouncementVO;
import com.ayor.entity.vo.TagVO;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.pojo.Threadd;
import com.ayor.mapper.*;
import com.ayor.service.AuthorizationService;
import com.ayor.service.ImageAssetService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.ThreaddService;
import com.ayor.type.ThreadOrderType;
import com.ayor.util.TipTapUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    private final AccountMapper accountMapper;

    private final TopicMapper topicMapper;

    private final PostMapper postMapper;

    private final TipTapUtils tipTapUtils;

    private final TagMapper tagMapper;

    private final MentionMessageService mentionMessageService;

    private final ImageAssetService imageAssetService;

    private final AuthorizationService authorizationService;
    /**
     * 获取指定主题下的帖子列表或分页结果。
     */

    @Override
    public List<ThreadVO> getThreadVOsByTopicId(Integer topicId) {
        if (topicId == null) {
            return null;
        }
        if (topicMapper.isTopicDelete(topicId)) {
            return null;
        }
        List<Threadd> threads = this.baseMapper.getThreadsByTopicId(topicId);
        return toVOs(threads);
    }
    /**
     * 获取指定主题下的帖子列表或分页结果。
     */

    @Override
    public PageEntity<ThreadVO> getThreadVOsByTopicId(Integer topicId, Integer tagId, Boolean isSelected, String order, Integer pageNum, Integer pageSize) {
        if (topicId == null) {
            return null;
        }
        if (topicMapper.isTopicDelete(topicId)) {
            return null;
        }
        LambdaQueryWrapper<Threadd> queryWrapper = new LambdaQueryWrapper<Threadd>()
                .eq(Threadd::getTopicId, topicId)
                .eq(Threadd::getIsDeleted, false)
                .eq(tagId != null, Threadd::getTagId, tagId)
                .eq(isSelected != null, Threadd::getIsSelected, isSelected);
        applyThreadOrder(queryWrapper, normalizeThreadOrder(order));
        Page<Threadd> threads = this.page(Page.of(pageNum, pageSize), queryWrapper);

        return new PageEntity<>(threads.getTotal(), toVOs(threads.getRecords()));
    }
    /**
     * 根据帖子 ID 获取标题。
     */

    @Override
    public String getThreadTitleById(Integer threadId) {
        if (threadId == null || !existsThreadById(threadId)) {
            return null;
        }
        Threadd threadd = this.baseMapper.selectById(threadId);
        return threadd.getTitle();
    }
    /**
     * 根据帖子 ID 获取帖子详情。
     */

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
    /**
     * 分页获取用户发布的帖子列表。
     */

    @Override
    public PageEntity<ThreadVO> getThreadPagesByUserId(Integer accountId, Integer currentPage, Integer pageSize) {
        Page<Threadd> page = new Page<>(currentPage, pageSize);
        Page<Threadd> threads = this.lambdaQuery()
                .eq(Threadd::getAccountId, accountId)
                .orderByAsc(Threadd::getCreateTime)
                .eq(Threadd::getIsDeleted, false)
                .page(page);
        List<ThreadVO> threadVOS = toVOs(threads.getRecords());
        Long totalPages = threads.getTotal();
        return new PageEntity<>(totalPages, threadVOS);
    }
    /**
     * 将帖子实体列表转换为视图对象列表。
     */

    @NotNull
    private List<ThreadVO> toVOs(List<Threadd> threads) {
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
                threadVO.setContent(tipTapUtils.filterNonImage(threadd.getContent()));
                threadVO.setImageUrls(tipTapUtils.extractImageUrls(threadd.getContent()));
                threadVO.setAvatarUrl(account.getAvatarUrl());
                threadVO.setAccountId(account.getAccountId());

                threadVOList.add(threadVO);
            }
        });
        return threadVOList;
    }

    private ThreadOrderType normalizeThreadOrder(String order) {
        return ThreadOrderType.fromValue(order);
    }

    private void applyThreadOrder(LambdaQueryWrapper<Threadd> queryWrapper, ThreadOrderType orderType) {
        switch (orderType) {
            case LATEST -> queryWrapper.orderByDesc(Threadd::getCreateTime);
            case LIKES -> queryWrapper.orderByDesc(Threadd::getLikeCount)
                    .orderByDesc(Threadd::getCreateTime);
            case COLLECTS -> queryWrapper.orderByDesc(Threadd::getCollectCount)
                    .orderByDesc(Threadd::getCreateTime);
            case VIEWS -> queryWrapper.orderByDesc(Threadd::getViewCount)
                    .orderByDesc(Threadd::getCreateTime);
            case REPLIES -> queryWrapper.orderByDesc(Threadd::getPostCount)
                    .orderByDesc(Threadd::getCreateTime);
            case HOT -> queryWrapper.orderByDesc(Threadd::getLikeCount)
                    .orderByDesc(Threadd::getPostCount)
                    .orderByDesc(Threadd::getViewCount)
                    .orderByDesc(Threadd::getCreateTime);
        }
    }
    /**
     * 校验作者后删除帖子。
     */

    @Override
    public String removeThreadById(Integer threadId, Integer accountId) {
        Threadd thread = this.getById(threadId);
        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            return "用户不存在";
        }
        authorizationService.assertCanDeleteThread(accountId, threadId);
        if (thread.getIsDeleted()) {
            return "帖子已删除";
        }
        imageAssetService.clearContentRefs("THREAD", threadId);
        postMapper.getPostsByThreadId(threadId).forEach(post -> imageAssetService.clearContentRefs("POST", post.getPostId()));
        postMapper.removePostsByThreadId(threadId);
        return this.removeByIdLogical(threadId) ? null : "删除失败";
    }
    /**
     * 管理员直接删除帖子。
     */

    public String permRemoveThreadById(Integer threadId) {
        Threadd thread = this.getById(threadId);
        if (thread == null) {
            return "帖子不存在";
        }
        if (thread.getIsDeleted()) {
            return "帖子已删除";
        }
        imageAssetService.clearContentRefs("THREAD", threadId);
        postMapper.getPostsByThreadId(threadId).forEach(post -> imageAssetService.clearContentRefs("POST", post.getPostId()));
        postMapper.removePostsByThreadId(threadId);
        return this.removeByIdLogical(threadId) ? null : "删除失败";
    }
    /**
     * 将帖子设置为主题公告。
     */

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
    /**
     * 取消帖子公告状态。
     */

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
    /**
     * 获取主题下的公告帖子列表。
     */

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
    /**
     * 创建帖子并同步写入索引与统计。
     */

    @Override
    public String insertThread(ThreadDTO threadDTO, Integer accountId) {
        if (accountId == null) {
            return "用户不存在";
        }
        Threadd threadd = new Threadd();
        BeanUtils.copyProperties(threadDTO, threadd);
        try {
            threadd.setContent(tipTapUtils.convertBase64ImagesToUrl(threadDTO.getContent(), "threads/" + threadd.getTopicId() + "/"));
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
        threadd.setAccountId(accountId);
        threadd.setCreateTime(new Date());

        if (this.save(threadd)) {
            imageAssetService.syncContentRefs("THREAD", threadd.getThreadId(), threadd.getContent(), accountId);
            mentionMessageService.createThreadMentionMessages(threadd.getContent(), accountId, threadd.getThreadId());
            return null;
        }
        return "添加失败";
    }
    /**
     * 更新帖子标签信息。
     */

    @Override
    public String updateThreadTag(Integer threadId, Integer topicId, Integer tagId) {
        if (threadId == null || topicId == null || tagId == null) {
            return "参数错误";
        }
        Threadd threadd = this.lambdaQuery()
                .eq(Threadd::getThreadId, threadId)
                .eq(Threadd::getTopicId, topicId)
                .one();
        if (threadd == null) {
            return "帖子不存在";
        }
        Tag tag = tagMapper.getTagById(tagId);
        if (tag == null) {
            return "标签不存在";
        }
        threadd.setTagId(tagId);

        return this.updateById(threadd) ? null : "修改失败";
    }
    /**
     * 删除帖子上的标签。
     */

    @Override
    public String removeThreadTag(Integer threadId, Integer topicId) {
        if (!existsThreadById(threadId)) {
            return "帖子不存在";
        }
        return this.baseMapper.removeThreadTag(threadId, topicId) ? null : "修改失败";
    }
    /**
     * 刷新帖子统计信息。
     */

    @Override
    public void updateThreadStat() {
        this.baseMapper.updateThreadPostCount();
        this.baseMapper.updateLikeCount();
    }
    /**
     * 增加帖子的浏览次数。
     */

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
    /**
     * 将帖子实体列表转换为搜索文档列表。
     */

    @Override
    public List<ThreadDoc> toThreadDocs(List<Threadd>     threads) {
        List<ThreadDoc> threadDocs = new ArrayList<>();
        threads.forEach(thread -> {
            ThreadDoc threadDoc = new ThreadDoc();
            BeanUtils.copyProperties(thread, threadDoc);
            threadDoc.setContent(tipTapUtils.extractText(thread.getContent()));
            threadDoc.setId("THREAD_"+thread.getThreadId());
            threadDoc.setIsThreadTopic(true);
            threadDocs.add(threadDoc);
        });
        return threadDocs;
    }
    /**
     * 判断帖子是否存在。
     */

    private boolean existsThreadById(Integer threadId) {
        Threadd threadd = this.lambdaQuery().eq(Threadd::getThreadId, threadId).one();
        return threadd != null && !threadd.getIsDeleted();
    }
    /**
     * 将帖子标记为逻辑删除。
     */

    private boolean removeByIdLogical(Serializable Id) {
        Threadd threadd = this.getById(Id);
        if (threadd == null) {
            return false;
        }
        threadd.setIsDeleted(true);
        return this.updateById(threadd);
    }

}
