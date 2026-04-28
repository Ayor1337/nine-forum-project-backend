package com.ayor.service;

/**
 * 隐私策略判定服务。
 */
public interface PrivacyPolicyService {

    /**
     * 判断是否允许查看用户资料。
     *
     * @param viewerId 当前查看者用户ID
     * @param ownerId 资料拥有者用户ID
     * @return true=允许查看,false=不允许查看
     */
    boolean canViewProfile(Integer viewerId, Integer ownerId);

    /**
     * 判断是否允许查看用户点赞列表。
     *
     * @param viewerId 当前查看者用户ID
     * @param ownerId 资料拥有者用户ID
     * @return true=允许查看,false=不允许查看
     */
    boolean canViewLikedThreads(Integer viewerId, Integer ownerId);

    /**
     * 判断是否允许查看用户收藏列表。
     *
     * @param viewerId 当前查看者用户ID
     * @param ownerId 资料拥有者用户ID
     * @return true=允许查看,false=不允许查看
     */
    boolean canViewCollectedThreads(Integer viewerId, Integer ownerId);

    /**
     * 判断是否允许查看用户粉丝列表。
     *
     * @param viewerId 当前查看者用户ID
     * @param ownerId 资料拥有者用户ID
     * @return true=允许查看,false=不允许查看
     */
    boolean canViewFollowerList(Integer viewerId, Integer ownerId);

    /**
     * 判断是否允许查看用户关注列表。
     *
     * @param viewerId 当前查看者用户ID
     * @param ownerId 资料拥有者用户ID
     * @return true=允许查看,false=不允许查看
     */
    boolean canViewFollowingList(Integer viewerId, Integer ownerId);

    /**
     * 判断是否允许发起私信会话。
     *
     * @param viewerId 当前发起者用户ID
     * @param ownerId 目标用户ID
     * @return true=允许发起,false=不允许发起
     */
    boolean canStartConversation(Integer viewerId, Integer ownerId);
}
