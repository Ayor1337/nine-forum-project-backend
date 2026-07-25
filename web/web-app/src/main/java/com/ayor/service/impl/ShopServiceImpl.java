package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.CreditAccount;
import com.ayor.entity.pojo.CreditTransaction;
import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.pojo.ShopOrder;
import com.ayor.entity.pojo.UserItem;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.UserItemVO;
import com.ayor.mapper.CreditAccountMapper;
import com.ayor.mapper.CreditTransactionMapper;
import com.ayor.mapper.ShopItemMapper;
import com.ayor.mapper.ShopOrderMapper;
import com.ayor.mapper.UserItemMapper;
import com.ayor.service.ShopService;
import com.ayor.type.CreditChangeType;
import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
import com.ayor.type.ShopOrderStatus;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 商城服务实现（用户端）
 *
 * 购买流程在同一事务内完成：校验商品与持有情况 → 行锁校验余额 →
 * 原子扣库存 → 扣款写流水 → 写订单 → 写入背包。
 * 所有失败分支都发生在任何写操作之前，异常则整体回滚。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopItemMapper, ShopItem> implements ShopService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserItemMapper userItemMapper;

    private final ShopOrderMapper shopOrderMapper;

    private final CreditAccountMapper creditAccountMapper;

    private final CreditTransactionMapper creditTransactionMapper;

    @Override
    public PageEntity<ShopItemVO> listOnSaleItems(Integer pageNum, Integer pageSize) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        return new PageEntity<>(
                baseMapper.countOnSaleItems(),
                baseMapper.selectOnSaleItems(offset, normalizedPageSize));
    }

    @Override
    public String purchase(Integer accountId, Integer itemId) {
        if (accountId == null || itemId == null) {
            return "参数错误";
        }
        ShopItem item = this.getById(itemId);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            return "商品不存在";
        }
        if (!Objects.equals(item.getStatus(), ShopItemStatus.LISTED.getCode())) {
            return "商品已下架";
        }
        // 装饰类商品通过 uk_user_item 唯一键天然限购一件，purchase_limit 预留未来堆叠道具
        Long owned = userItemMapper.selectCount(Wrappers.<UserItem>lambdaQuery()
                .eq(UserItem::getAccountId, accountId)
                .eq(UserItem::getItemId, itemId));
        if (owned != null && owned > 0) {
            return "已拥有该商品";
        }
        creditAccountMapper.initAccount(accountId);
        CreditAccount creditAccount = creditAccountMapper.selectForUpdate(accountId);
        if (creditAccount.getBalance() < item.getPrice()) {
            return "余额不足，当前余额为 " + creditAccount.getBalance();
        }
        if (baseMapper.decreaseStock(itemId) == 0) {
            return "库存不足";
        }
        creditAccountMapper.updateBalance(accountId, -item.getPrice());
        CreditTransaction transaction = new CreditTransaction();
        transaction.setAccountId(accountId);
        transaction.setDelta(-item.getPrice());
        transaction.setBalanceAfter(creditAccount.getBalance() - item.getPrice());
        transaction.setChangeType(CreditChangeType.PURCHASE.getType());
        transaction.setRemark("购买商品：" + item.getName());
        transaction.setOperatorId(accountId);
        creditTransactionMapper.insert(transaction);
        ShopOrder order = new ShopOrder();
        order.setAccountId(accountId);
        order.setItemId(itemId);
        order.setPrice(item.getPrice());
        order.setQuantity(1);
        order.setStatus(ShopOrderStatus.SUCCESS.getCode());
        shopOrderMapper.insert(order);
        UserItem userItem = new UserItem();
        userItem.setAccountId(accountId);
        userItem.setItemId(itemId);
        userItem.setQuantity(1);
        userItem.setIsEquipped(false);
        userItemMapper.insert(userItem);
        return null;
    }

    @Override
    public PageEntity<UserItemVO> listMyItems(Integer accountId, Integer pageNum, Integer pageSize) {
        if (accountId == null) {
            return null;
        }
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        return new PageEntity<>(
                userItemMapper.countByAccountId(accountId),
                userItemMapper.selectByAccountId(accountId, offset, normalizedPageSize));
    }

    @Override
    public String updateEquipment(Integer accountId, Long userItemId, Boolean equipped) {
        if (accountId == null || userItemId == null || equipped == null) {
            return "参数错误";
        }
        UserItem userItem = userItemMapper.selectById(userItemId);
        if (userItem == null || !accountId.equals(userItem.getAccountId())) {
            return "装饰不存在";
        }
        if (equipped) {
            ShopItem item = this.getById(userItem.getItemId());
            ShopItemType itemType = item == null ? null : ShopItemType.fromType(item.getItemType());
            if (itemType != null && itemType.isSingleEquip()) {
                userItemMapper.unequipByType(accountId, itemType.getType());
            }
            userItem.setIsEquipped(true);
        } else {
            userItem.setIsEquipped(false);
        }
        userItemMapper.updateById(userItem);
        return null;
    }

    @Override
    public List<UserItemVO> listEquippedDecorations(Integer accountId) {
        if (accountId == null) {
            return null;
        }
        return userItemMapper.selectEquippedByAccountId(accountId);
    }
}
