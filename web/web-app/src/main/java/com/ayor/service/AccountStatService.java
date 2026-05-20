package com.ayor.service;

import com.ayor.entity.vo.AccountStatVO;
import com.ayor.entity.pojo.AccountStat;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户统计服务接口
 *
 * 提供用户统计数据的查询和更新功能。
 *
 * 主要功能:
 * - 统计查询: 获取用户的统计数据(帖子数、评论数、粉丝数等)
 * - 统计更新: 定时更新用户统计数据
 *
 * @see AccountStat 用户统计实体
 * @see AccountStatVO 用户统计视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface AccountStatService extends IService<AccountStat> {

    /**
     * 获取用户的统计数据
     * @param accountId 用户ID
     * @return 用户统计视图对象,包含帖子数、评论数、粉丝数等统计信息
     */
    AccountStatVO getAccountStatByUserId(Integer accountId);

    /**
     * 更新所有用户的统计数据(定时任务调用)
     * @note 此方法由定时任务触发,不应手动调用
     */
    void updateAccountStat();
}
