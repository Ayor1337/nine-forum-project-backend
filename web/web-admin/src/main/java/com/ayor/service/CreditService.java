package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.CreditAdjustDTO;
import com.ayor.entity.pojo.CreditAccount;
import com.ayor.entity.vo.CreditBalanceVO;
import com.ayor.entity.vo.CreditTransactionVO;
import com.ayor.type.CreditChangeType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Credit 货币管理服务接口（管理端）
 *
 * 提供管理员发放/扣减 Credit、查询余额与流水的功能。
 */
public interface CreditService extends IService<CreditAccount> {

    /**
     * 调整用户 Credit 余额并记录流水
     * @param operatorId 操作管理员账号ID
     * @param dto 调整参数（账号ID、数量、备注）
     * @return 成功返回 null，失败返回错误消息
     */
    String adjustCredit(Integer operatorId, CreditAdjustDTO dto);

    /**
     * 查询指定用户的 Credit 余额
     * @param accountId 用户ID
     * @return 余额视图对象，用户不存在时返回 null
     */
    CreditBalanceVO getBalance(Integer accountId);

    /**
     * 分页查询 Credit 流水，支持按用户、类型筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param accountId 用户ID（可选）
     * @param username 用户名（可选）
     * @param changeType 变动类型（可选）
     * @param sortOrder 排序方向（asc/desc）
     * @return 流水分页数据
     */
    PageEntity<CreditTransactionVO> listTransactions(Integer pageNum,
                                                     Integer pageSize,
                                                     Integer accountId,
                                                     String username,
                                                     CreditChangeType changeType,
                                                     String sortOrder);
}
