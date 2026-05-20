package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.AccountStat;
import com.ayor.entity.vo.AccountStatVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户统计管理服务接口(管理员版)
 *
 * 提供后台用户统计数据管理功能,包括统计查询和更新等管理员专用操作。
 *
 * 主要功能:
 * - 统计查询: 按用户分页查询统计数据
 * - 统计管理: 更新用户统计数据
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see AccountStat 用户统计实体
 * @author ayor
 * @since 1.0.0
 */
public interface AccountStatService extends IService<AccountStat> {

    /**
     * 获取用户统计数据列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param accountId 用户ID过滤条件;为null时查询所有用户
     * @return 分页结果,包含用户统计数据
     */
    PageEntity<AccountStatVO> getAccountStats(Integer pageNum, Integer pageSize, Integer accountId);

    /**
     * 获取单条用户统计记录
     * @param statId 统计记录ID
     * @return 用户统计记录
     */
    AccountStatVO getAccountStatById(Integer statId);

    /**
     * 创建用户统计记录
     * @param accountStat 用户统计记录
     * @return 操作结果消息
     */
    String createAccountStat(AccountStat accountStat);

    /**
     * 更新用户统计数据
     * @param statId 统计记录ID
     * @param accountStat 用户统计实体对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateAccountStat(Integer statId, AccountStat accountStat);

    /**
     * 删除用户统计记录
     * @param statId 统计记录ID
     * @return 操作结果消息
     */
    String deleteAccountStat(Integer statId);
}
