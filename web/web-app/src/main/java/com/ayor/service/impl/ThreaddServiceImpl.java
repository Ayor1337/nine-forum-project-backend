package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.Base64Upload;
import com.ayor.entity.document.ThreadDoc;
import com.ayor.entity.dto.ThreadDTO;
import com.ayor.entity.pojo.Announcement;
import com.ayor.entity.vo.AnnouncementVO;
import com.ayor.entity.vo.TagVO;
import com.ayor.entity.vo.ThreadEditHistoryDetailVO;
import com.ayor.entity.vo.ThreadEditHistoryVO;
import com.ayor.entity.vo.ThreadBreadcrumbVO;
import com.ayor.entity.vo.ThreadVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Tag;
import com.ayor.entity.pojo.Threadd;
import com.ayor.entity.pojo.Topic;
import com.ayor.entity.pojo.ThreadEditHistory;
import com.ayor.mapper.*;
import com.ayor.service.AuthorizationService;
import com.ayor.service.CacheInvalidationService;
import com.ayor.mq.EsIndexSyncProducer;
import com.ayor.service.FollowMessageService;
import com.ayor.service.ForumRealtimeService;
import com.ayor.service.ImageAssetService;
import com.ayor.service.MentionMessageService;
import com.ayor.service.ThreaddService;
import com.ayor.service.UserRelationService;
import com.ayor.type.ThreadOrderType;
import com.ayor.type.ThreadRankingMetric;
import com.ayor.type.ThreadRankingPeriod;
import com.ayor.util.TipTapUtils;
import com.ayor.image.ImageStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 帖子服务实现
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ThreaddServiceImpl extends ServiceImpl<ThreaddMapper, Threadd> implements ThreaddService {

    private static final int MAX_THREAD_IMAGE_COUNT = 7;
    private static final String THREAD_IMAGE_LIMIT_ERROR = "帖子最多只能包含7张图片";

    private final AccountMapper accountMapper;

    private final AnnouncementMapper announcementMapper;

    private final TopicMapper topicMapper;

    private final PostMapper postMapper;

    private final TipTapUtils tipTapUtils;

    private final TagMapper tagMapper;

    private final MentionMessageService mentionMessageService;

    private final FollowMessageService followMessageService;

    private final ForumRealtimeService forumRealtimeService;

    private final ImageAssetService imageAssetService;

    private final ImageStorageService imageStorageService;

    private final AuthorizationService authorizationService;

    private final UserRelationService userRelationService;

    private final ThreadEditHistoryMapper threadEditHistoryMapper;

    private final CacheInvalidationService cacheInvalidationService;

    private final EsIndexSyncProducer esIndexSyncProducer;
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
    public PageEntity<ThreadVO> getThreadVOsByTopicId(Integer viewerId, Integer topicId, Integer tagId, Boolean isSelected, String order, Integer pageNum, Integer pageSize) {
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
        applyBlockedAuthorFilter(queryWrapper, viewerId);
        applyThreadOrder(queryWrapper, normalizeThreadOrder(order));
        Page<Threadd> threads = this.page(Page.of(pageNum, pageSize), queryWrapper);

        return new PageEntity<>(threads.getTotal(), toVOs(threads.getRecords()));
    }

    @Override
    @Cacheable(value = "threadRanking",
            key = "'topic:' + #viewerId + ':' + #topicId + ':' + #period + ':' + #metric + ':' + #pageNum + ':' + #pageSize",
            condition = "#topicId != null && #pageNum != null && #pageSize != null",
            unless = "#result == null || #result.totalSize == 0")
    public PageEntity<ThreadVO> getThreadRankingsByTopicId(Integer viewerId, Integer topicId, String period, String metric, Integer pageNum, Integer pageSize) {
        if (topicId == null) {
            return null;
        }
        if (topicMapper.isTopicDelete(topicId)) {
            return null;
        }
        LambdaQueryWrapper<Threadd> queryWrapper = buildRankingQuery(period, metric)
                .eq(Threadd::getTopicId, topicId);
        applyBlockedAuthorFilter(queryWrapper, viewerId);
        Page<Threadd> threads = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return new PageEntity<>(threads.getTotal(), toVOs(threads.getRecords()));
    }

    @Override
    @Cacheable(value = "threadRanking",
            key = "'all:' + #viewerId + ':' + #period + ':' + #metric + ':' + #pageNum + ':' + #pageSize",
            condition = "#pageNum != null && #pageSize != null",
            unless = "#result == null || #result.totalSize == 0")
    public PageEntity<ThreadVO> getThreadRankings(Integer viewerId, String period, String metric, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Threadd> queryWrapper = buildRankingQuery(period, metric);
        applyBlockedAuthorFilter(queryWrapper, viewerId);
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

    @Override
    public ThreadBreadcrumbVO getThreadBreadcrumbById(Integer threadId) {
        if (threadId == null) {
            return null;
        }
        Threadd threadd = this.baseMapper.selectById(threadId);
        if (threadd == null || Boolean.TRUE.equals(threadd.getIsDeleted())) {
            return null;
        }
        Topic topic = topicMapper.selectById(threadd.getTopicId());
        if (topic == null || Boolean.TRUE.equals(topic.getIsDeleted())) {
            return null;
        }
        return new ThreadBreadcrumbVO(threadd.getTitle(), topic.getTitle());
    }
    /**
     * 根据帖子 ID 获取帖子详情。
     */

    @Override
    public ThreadVO getThreadById(Integer viewerId, Integer threadId) {
        if (threadId == null) {
            return null;
        }
        Threadd threadd = this.baseMapper.selectById(threadId);
        if (threadd == null || Boolean.TRUE.equals(threadd.getIsDeleted())) {
            return null;
        }
        if (viewerId != null && !Objects.equals(viewerId, threadd.getAccountId())
                && userRelationService.isBlockedEitherDirection(viewerId, threadd.getAccountId())) {
            throw new AccessDeniedException("Access denied");
        }
        ThreadVO threadVO = new ThreadVO();
        TagVO tagVO = new TagVO();
        Account account = accountMapper.getAccountById(threadd.getAccountId());
        Tag tag = tagMapper.getTagById(threadd.getTagId());

        if (tag != null) {
            BeanUtils.copyProperties(tag, tagVO);
        }
        BeanUtils.copyProperties(threadd, threadVO);
        threadVO.setImageUrls(normalizeImageUrls(threadd.getImagesUrls()));


        threadVO.setTag(tagVO);
        threadVO.setAccountName(account.getNickname());
        threadVO.setAvatarUrl(account.getAvatarUrl());
        threadVO.setAccountId(account.getAccountId());
        threadVO.setEditCount(countEdits(threadId));
        return threadVO;
    }
    /**
     * 分页获取用户发布的帖子列表。
     */

    @Override
    public PageEntity<ThreadVO> getThreadPagesByUserId(Integer viewerId, Integer accountId, Integer currentPage, Integer pageSize) {
        if (viewerId != null && !Objects.equals(viewerId, accountId)
                && userRelationService.isBlockedEitherDirection(viewerId, accountId)) {
            throw new AccessDeniedException("Access denied");
        }
        Page<Threadd> page = new Page<>(currentPage, pageSize);
        LambdaQueryWrapper<Threadd> queryWrapper = new LambdaQueryWrapper<Threadd>()
                .eq(Threadd::getAccountId, accountId)
                .orderByAsc(Threadd::getCreateTime)
                .eq(Threadd::getIsDeleted, false);
        Page<Threadd> threads = this.page(page, queryWrapper);
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
                threadVO.setImageUrls(normalizeImageUrls(threadd.getImagesUrls()));
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

    private LambdaQueryWrapper<Threadd> buildRankingQuery(String period, String metric) {
        LambdaQueryWrapper<Threadd> queryWrapper = new LambdaQueryWrapper<Threadd>()
                .eq(Threadd::getIsDeleted, false)
                .ge(Threadd::getCreateTime, ThreadRankingPeriod.fromValue(period).getStartTime());
        applyRankingMetricOrder(queryWrapper, ThreadRankingMetric.fromValue(metric));
        return queryWrapper;
    }

    private void applyBlockedAuthorFilter(LambdaQueryWrapper<Threadd> queryWrapper, Integer viewerId) {
        if (viewerId == null) {
            return;
        }
        List<Integer> blockedAccountIds = userRelationService.listBlockedAccountIdsEitherDirection(viewerId);
        if (!blockedAccountIds.isEmpty()) {
            queryWrapper.notIn(Threadd::getAccountId, blockedAccountIds);
        }
    }

    private void applyRankingMetricOrder(LambdaQueryWrapper<Threadd> queryWrapper, ThreadRankingMetric metric) {
        switch (metric) {
            case LIKES -> queryWrapper.orderByDesc(Threadd::getLikeCount)
                    .orderByDesc(Threadd::getCreateTime);
            case VIEWS -> queryWrapper.orderByDesc(Threadd::getViewCount)
                    .orderByDesc(Threadd::getCreateTime);
            case COLLECTS -> queryWrapper.orderByDesc(Threadd::getCollectCount)
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
        if (!this.removeByIdLogical(threadId)) {
            return "删除失败";
        }
        cacheInvalidationService.clearThreadRanking();
        esIndexSyncProducer.syncThread(threadId);
        return null;
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
        if (!this.removeByIdLogical(threadId)) {
            return "删除失败";
        }
        cacheInvalidationService.clearThreadRanking();
        esIndexSyncProducer.syncThread(threadId);
        return null;
    }
    /**
     * 将帖子设置为主题公告。
     */

    @Override
    public String setAnnouncementByThreadId(Integer threadId, Integer topicId) {
        Threadd thread = this.baseMapper.selectOne(new LambdaQueryWrapper<Threadd>()
                .eq(Threadd::getThreadId, threadId)
                .eq(Threadd::getTopicId, topicId));
        if (thread == null || thread.getIsDeleted()) {
            return "帖子不存在";
        }
        if (getAnnouncement(threadId, false) != null) {
            return "该帖子已经是公告";
        }
        Announcement announcement = new Announcement();
        announcement.setThreadId(threadId);
        announcement.setIsGlobal(false);
        announcement.setCreateTime(new Date());
        return announcementMapper.insert(announcement) > 0 ? null : "修改失败";
    }
    /**
     * 取消帖子公告状态。
     */

    @Override
    public String removeAnnouncementByThreadId(Integer threadId, Integer topicId) {
        Threadd thread = this.baseMapper.selectOne(new LambdaQueryWrapper<Threadd>()
                .eq(Threadd::getThreadId, threadId)
                .eq(Threadd::getTopicId, topicId));
        if (thread == null || thread.getIsDeleted()) {
            return "帖子不存在";
        }
        Announcement announcement = getAnnouncement(threadId, false);
        if (announcement == null) {
            return "该帖子不是公告";
        }
        return announcementMapper.deleteById(announcement.getAnnouncementId()) > 0 ? null : "修改失败";
    }

    @Override
    public String setGlobalAnnouncementByThreadId(Integer threadId) {
        Threadd thread = this.getById(threadId);
        if (thread == null || thread.getIsDeleted()) {
            return "帖子不存在";
        }
        if (getAnnouncement(threadId, true) != null) {
            return "该帖子已经是全局公告";
        }
        Announcement announcement = new Announcement();
        announcement.setThreadId(threadId);
        announcement.setIsGlobal(true);
        announcement.setCreateTime(new Date());
        return announcementMapper.insert(announcement) > 0 ? null : "修改失败";
    }

    @Override
    public String removeGlobalAnnouncementByThreadId(Integer threadId) {
        Threadd thread = this.getById(threadId);
        if (thread == null || thread.getIsDeleted()) {
            return "帖子不存在";
        }
        Announcement announcement = getAnnouncement(threadId, true);
        if (announcement == null) {
            return "该帖子不是全局公告";
        }
        return announcementMapper.deleteById(announcement.getAnnouncementId()) > 0 ? null : "修改失败";
    }
    /**
     * 获取主题下的公告帖子列表。
     */

    @Override
    public List<AnnouncementVO> getAnnouncementThreads(Integer topicId) {
        if (topicId == null) {
            return null;
        }
        return announcementMapper.getTopicAnnouncements(topicId);
    }

    @Override
    public List<AnnouncementVO> getGlobalAnnouncementThreads() {
        return announcementMapper.getGlobalAnnouncements();
    }

    private Announcement getAnnouncement(Integer threadId, Boolean isGlobal) {
        if (threadId == null || isGlobal == null) {
            return null;
        }
        return announcementMapper.selectOne(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getThreadId, threadId)
                .eq(Announcement::getIsGlobal, isGlobal));
    }
    /**
     * 创建帖子并异步同步搜索索引与统计。
     */

    @Override
    public String insertThread(ThreadDTO threadDTO, Integer accountId) {
        if (accountId == null) {
            return "用户不存在";
        }
        String contentError = validateThreadContent(threadDTO.getContent());
        if (contentError != null) {
            return contentError;
        }
        String imageUrlsError = validateThreadImageUrls(threadDTO.getImageUrls(), threadDTO.getImages());
        if (imageUrlsError != null) {
            return imageUrlsError;
        }
        String tagError = validateTagBelongsToTopic(threadDTO.getTagId(), threadDTO.getTopicId());
        if (tagError != null) {
            return tagError;
        }
        Threadd threadd = new Threadd();
        BeanUtils.copyProperties(threadDTO, threadd);
        threadd.setContent(threadDTO.getContent());
        threadd.setImagesUrls(mergeImageUrls(threadDTO.getImageUrls(), threadDTO.getImages(), threadImagePath(threadDTO.getTopicId())));
        threadd.setAccountId(accountId);
        threadd.setCreateTime(new Date());

        if (this.save(threadd)) {
            imageAssetService.syncContentRefs("THREAD", threadd.getThreadId(), threadd.getImagesUrls(), accountId);
            mentionMessageService.createThreadMentionMessages(threadd.getContent(), accountId, threadd.getThreadId());
            followMessageService.createThreadFollowMessages(threadd);
            cacheInvalidationService.clearThreadRanking();
            forumRealtimeService.publishThreadCreated(threadd);
            esIndexSyncProducer.syncThread(threadd.getThreadId());
            return null;
        }
        return "添加失败";
    }

    /**
     * 编辑帖子：先快照原标题与原正文，再以新值覆盖当前帖子。
     */
    @Override
    public String editThread(Integer threadId, ThreadDTO threadDTO, Integer accountId) {
        if (threadId == null || threadDTO == null) {
            return "参数错误";
        }
        authorizationService.assertCanEditThread(accountId, threadId);
        Threadd threadd = this.getById(threadId);
        if (threadd == null || Boolean.TRUE.equals(threadd.getIsDeleted())) {
            return "帖子不存在";
        }

        String contentError = validateThreadContent(threadDTO.getContent());
        if (contentError != null) {
            return contentError;
        }
        String imageUrlsError = validateThreadImageUrls(threadDTO.getImageUrls(), threadDTO.getImages());
        if (imageUrlsError != null) {
            return imageUrlsError;
        }
        String tagError = validateTagBelongsToTopic(threadDTO.getTagId(), threadd.getTopicId());
        if (tagError != null) {
            return tagError;
        }
        Integer oldTagId = threadd.getTagId();

        String newContent = threadDTO.getContent();
        List<String> imageUrls = mergeImageUrls(threadDTO.getImageUrls(), threadDTO.getImages(), threadImagePath(threadd.getTopicId()));

        ThreadEditHistory snapshot = new ThreadEditHistory();
        snapshot.setThreadId(threadId);
        snapshot.setEditorAccountId(accountId);
        snapshot.setTitle(threadd.getTitle());
        snapshot.setContent(threadd.getContent());
        snapshot.setEditTime(new Date());
        threadEditHistoryMapper.insert(snapshot);

        threadd.setTitle(threadDTO.getTitle());
        threadd.setContent(newContent);
        threadd.setImagesUrls(imageUrls);
        threadd.setUpdateTime(new Date());
        threadd.setTagId(threadDTO.getTagId());
        if (!this.updateById(threadd)) {
            return "编辑失败";
        }
        // updateById 默认忽略 null 字段，清除标签需要单独置空
        if (threadDTO.getTagId() == null && oldTagId != null) {
            this.baseMapper.removeThreadTag(threadId, threadd.getTopicId());
        }

        imageAssetService.syncContentRefs("THREAD", threadId, threadd.getImagesUrls(), accountId);
        mentionMessageService.createThreadMentionMessages(newContent, accountId, threadId);
        cacheInvalidationService.clearThreadRanking();
        esIndexSyncProducer.syncThread(threadId);
        return null;
    }

    /**
     * 校验帖子正文格式，并拒绝正文中的图片节点。
     */
    private String validateThreadContent(String content) {
        try {
            tipTapUtils.assertNoImageNodes(content);
            return null;
        } catch (IllegalArgumentException exception) {
            return exception.getMessage();
        }
    }

    private String validateThreadImageUrls(List<String> imageUrls, List<Base64Upload> images) {
        return normalizeImageUrls(imageUrls).size() + normalizeImages(images).size() > MAX_THREAD_IMAGE_COUNT
                ? THREAD_IMAGE_LIMIT_ERROR
                : null;
    }

    private List<String> normalizeImageUrls(List<String> imageUrls) {
        return imageUrls == null ? new ArrayList<>() : new ArrayList<>(imageUrls);
    }

    private List<Base64Upload> normalizeImages(List<Base64Upload> images) {
        return images == null ? new ArrayList<>() : new ArrayList<>(images);
    }

    private List<String> mergeImageUrls(List<String> imageUrls, List<Base64Upload> images, String path) {
        List<String> mergedImageUrls = normalizeImageUrls(imageUrls);
        mergedImageUrls.addAll(imageStorageService.storeImageBase64Images(normalizeImages(images), path));
        return mergedImageUrls;
    }

    private String threadImagePath(Integer topicId) {
        return "threads/" + topicId + "/";
    }

    /**
     * 校验标签存在且属于指定话题；tagId 为 null 时视为不设置标签，直接通过。
     */
    private String validateTagBelongsToTopic(Integer tagId, Integer topicId) {
        if (tagId == null) {
            return null;
        }
        Tag tag = tagMapper.getTagById(tagId);
        if (tag == null) {
            return "标签不存在";
        }
        if (!Objects.equals(tag.getTopicId(), topicId)) {
            return "标签不属于该主题";
        }
        return null;
    }

    @Override
    public Integer countEdits(Integer threadId) {
        if (threadId == null) {
            return 0;
        }
        Long count = threadEditHistoryMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ThreadEditHistory>()
                        .eq(ThreadEditHistory::getThreadId, threadId));
        return count == null ? 0 : count.intValue();
    }

    @Override
    public List<ThreadEditHistoryVO> listEditHistory(Integer threadId) {
        if (threadId == null) {
            return new ArrayList<>();
        }
        List<ThreadEditHistory> records = threadEditHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ThreadEditHistory>()
                        .eq(ThreadEditHistory::getThreadId, threadId)
                        .orderByDesc(ThreadEditHistory::getEditTime));
        List<ThreadEditHistoryVO> result = new ArrayList<>(records.size());
        for (ThreadEditHistory record : records) {
            ThreadEditHistoryVO vo = new ThreadEditHistoryVO();
            populateBaseHistoryVO(vo, record);
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<ThreadEditHistoryDetailVO> listEditHistoryWithSnapshots(Integer threadId) {
        if (threadId == null) {
            return new ArrayList<>();
        }
        List<ThreadEditHistory> records = threadEditHistoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ThreadEditHistory>()
                        .eq(ThreadEditHistory::getThreadId, threadId)
                        .orderByDesc(ThreadEditHistory::getEditTime));
        List<ThreadEditHistoryDetailVO> result = new ArrayList<>(records.size());
        for (ThreadEditHistory record : records) {
            ThreadEditHistoryDetailVO vo = new ThreadEditHistoryDetailVO();
            populateBaseHistoryVO(vo, record);
            vo.setTitle(record.getTitle());
            vo.setContent(record.getContent());
            result.add(vo);
        }
        return result;
    }

    private void populateBaseHistoryVO(ThreadEditHistoryVO vo, ThreadEditHistory record) {
        vo.setHistoryId(record.getHistoryId());
        vo.setThreadId(record.getThreadId());
        vo.setEditTime(record.getEditTime());
        vo.setEditorId(record.getEditorAccountId());
        Account editor = accountMapper.getAccountById(record.getEditorAccountId());
        if (editor != null) {
            vo.setEditorName(editor.getNickname());
            vo.setEditorAvatar(editor.getAvatarUrl());
        }
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

        if (!this.updateById(threadd)) {
            return "修改失败";
        }
        cacheInvalidationService.clearThreadRanking();
        return null;
    }
    /**
     * 删除帖子上的标签。
     */

    @Override
    public String removeThreadTag(Integer threadId, Integer topicId) {
        if (!existsThreadById(threadId)) {
            return "帖子不存在";
        }
        if (!this.baseMapper.removeThreadTag(threadId, topicId)) {
            return "修改失败";
        }
        cacheInvalidationService.clearThreadRanking();
        return null;
    }
    /**
     * 刷新帖子统计信息。
     */

    @Override
    public void updateThreadStat() {
        this.baseMapper.updateThreadPostCount();
        this.baseMapper.updateLikeCount();
        cacheInvalidationService.clearThreadRanking();
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
        boolean updated = this.updateById(threadd);
        lock.unlock();
        if (!updated) {
            return "更新失败";
        }
        cacheInvalidationService.clearThreadRanking();
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
            threadDoc.setAccountId(thread.getAccountId());
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
