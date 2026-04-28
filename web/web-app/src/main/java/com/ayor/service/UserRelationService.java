package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.pojo.UserRelation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户关系服务接口。
 */
public interface UserRelationService extends IService<UserRelation> {

    /**
     * 关注指定用户。
     *
     * @param fromAccountId 发起关注的用户ID
     * @param toAccountId 被关注用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String follow(Integer fromAccountId, Integer toAccountId);

    /**
     * 取消关注指定用户。
     *
     * @param fromAccountId 发起取消关注的用户ID
     * @param toAccountId 被取消关注用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String unfollow(Integer fromAccountId, Integer toAccountId);

    /**
     * 拉黑指定用户。
     *
     * @param fromAccountId 发起拉黑的用户ID
     * @param toAccountId 被拉黑用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String block(Integer fromAccountId, Integer toAccountId);

    /**
     * 取消拉黑指定用户。
     *
     * @param fromAccountId 发起取消拉黑的用户ID
     * @param toAccountId 被取消拉黑用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String unblock(Integer fromAccountId, Integer toAccountId);

    /**
     * 判断两个用户之间是否存在关注关系。
     *
     * @param fromAccountId 发起用户ID
     * @param toAccountId 目标用户ID
     * @return true=已关注,false=未关注
     */
    boolean isFollowing(Integer fromAccountId, Integer toAccountId);

    /**
     * 判断两个用户是否互相关注。
     *
     * @param firstAccountId 第一个用户ID
     * @param secondAccountId 第二个用户ID
     * @return true=互相关注,false=否则
     */
    boolean isMutualFollowing(Integer firstAccountId, Integer secondAccountId);

    /**
     * 判断两个用户之间是否存在任一方向的拉黑关系。
     *
     * @param firstAccountId 第一个用户ID
     * @param secondAccountId 第二个用户ID
     * @return true=存在拉黑,false=不存在拉黑
     */
    boolean isBlockedEitherDirection(Integer firstAccountId, Integer secondAccountId);

    /**
     * 获取用户粉丝列表。
     *
     * @param accountId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 粉丝列表分页结果
     */
    PageEntity<UserInfoVO> getFollowers(Integer accountId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户关注列表。
     *
     * @param accountId 用户ID
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 关注列表分页结果
     */
    PageEntity<UserInfoVO> getFollowings(Integer accountId, Integer pageNum, Integer pageSize);
}
