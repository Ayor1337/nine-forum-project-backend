package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.CreditAccount;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.entity.vo.RecentCheckInUserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Credit 货币查询服务接口（用户端）
 *
 * 提供用户查询本人 Credit 余额与流水的功能。
 */
public interface CreditService extends IService<CreditAccount> {

    /**
     * 查询用户 Credit 余额，无记录时返回 0
     * @param accountId 用户ID
     * @return 余额视图对象
     */
    CreditBalanceVO getBalance(Integer accountId);

    /**
     * 当前用户每日签到并领取 Credit。
     *
     * @param accountId 用户ID
     * @return 成功返回 null，失败返回错误消息
     */
    String checkIn(Integer accountId);

    /**
     * 查询最近签到的有效用户。
     *
     * @return 最多五位用户的签到信息
     */
    List<RecentCheckInUserVO> listRecentCheckInUsers();

    /**
     * 查询用户在当前东京业务日是否已签到。
     *
     * @param accountId 用户 ID
     * @return 已签到时为 true，否则为 false
     */
    boolean hasCheckedInToday(Integer accountId);

    /**
     * 分页查询用户本人的 Credit 流水（按时间倒序）
     * @param accountId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 流水分页数据
     */
    PageEntity<CreditTransactionVO> listMyTransactions(Integer accountId, Integer pageNum, Integer pageSize);
}
