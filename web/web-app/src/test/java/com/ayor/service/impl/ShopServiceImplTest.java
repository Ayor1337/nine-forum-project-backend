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
import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
import com.ayor.type.ShopOrderStatus;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest {

    @Mock
    private ShopItemMapper shopItemMapper;

    @Mock
    private UserItemMapper userItemMapper;

    @Mock
    private ShopOrderMapper shopOrderMapper;

    @Mock
    private CreditAccountMapper creditAccountMapper;

    @Mock
    private CreditTransactionMapper creditTransactionMapper;

    private ShopServiceImpl shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopServiceImpl(userItemMapper, shopOrderMapper, creditAccountMapper, creditTransactionMapper);
        ReflectionTestUtils.setField(shopService, "baseMapper", shopItemMapper);
    }

    // 测试购买成功时扣款、流水、订单与背包一致
    @Test
    void shouldPurchaseSuccessfully() {
        ShopItem item = shopItem(3, "头像框·星轨", ShopItemType.AVATAR_FRAME, 200L, 5L);
        when(shopItemMapper.selectById(3)).thenReturn(item);
        when(userItemMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount(7, 500L));
        when(shopItemMapper.decreaseStock(3)).thenReturn(1);

        String result = shopService.purchase(7, 3);

        assertThat(result).isNull();
        verify(creditAccountMapper).updateBalance(7, -200L);
        CreditTransaction transaction = captured(creditTransactionMapper);
        assertThat(transaction.getDelta()).isEqualTo(-200L);
        assertThat(transaction.getBalanceAfter()).isEqualTo(300L);
        assertThat(transaction.getChangeType()).isEqualTo("purchase");
        assertThat(transaction.getRemark()).isEqualTo("购买商品：头像框·星轨");
        assertThat(transaction.getOperatorId()).isEqualTo(7);
        ShopOrder order = captured(shopOrderMapper);
        assertThat(order.getPrice()).isEqualTo(200L);
        assertThat(order.getQuantity()).isEqualTo(1);
        assertThat(order.getStatus()).isEqualTo(ShopOrderStatus.SUCCESS.getCode());
        UserItem userItem = captured(userItemMapper);
        assertThat(userItem.getAccountId()).isEqualTo(7);
        assertThat(userItem.getItemId()).isEqualTo(3);
        assertThat(userItem.getQuantity()).isEqualTo(1);
        assertThat(userItem.getIsEquipped()).isFalse();
    }

    // 测试商品不存在或已删除时拒绝购买
    @Test
    void shouldRejectPurchaseWhenItemMissingOrDeleted() {
        when(shopItemMapper.selectById(3)).thenReturn(null);
        assertThat(shopService.purchase(7, 3)).isEqualTo("商品不存在");

        ShopItem deleted = shopItem(4, "勋章", ShopItemType.BADGE, 100L, -1L);
        deleted.setIsDeleted(true);
        when(shopItemMapper.selectById(4)).thenReturn(deleted);
        assertThat(shopService.purchase(7, 4)).isEqualTo("商品不存在");

        verify(shopItemMapper, never()).decreaseStock(anyInt());
    }

    // 测试商品已下架时拒绝购买
    @Test
    void shouldRejectPurchaseWhenItemDelisted() {
        ShopItem item = shopItem(3, "勋章", ShopItemType.BADGE, 100L, -1L);
        item.setStatus(ShopItemStatus.DELISTED.getCode());
        when(shopItemMapper.selectById(3)).thenReturn(item);

        assertThat(shopService.purchase(7, 3)).isEqualTo("商品已下架");
        verify(shopItemMapper, never()).decreaseStock(anyInt());
    }

    // 测试已拥有商品时拒绝重复购买且不影响库存与余额
    @Test
    void shouldRejectPurchaseWhenAlreadyOwned() {
        when(shopItemMapper.selectById(3)).thenReturn(shopItem(3, "勋章", ShopItemType.BADGE, 100L, 5L));
        when(userItemMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertThat(shopService.purchase(7, 3)).isEqualTo("已拥有该商品");
        verify(shopItemMapper, never()).decreaseStock(anyInt());
        verify(creditAccountMapper, never()).updateBalance(anyInt(), anyLong());
    }

    // 测试余额不足时拒绝购买且不扣库存
    @Test
    void shouldRejectPurchaseWhenBalanceInsufficient() {
        when(shopItemMapper.selectById(3)).thenReturn(shopItem(3, "头衔·论坛之星", ShopItemType.TITLE, 1000L, -1L));
        when(userItemMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount(7, 500L));

        assertThat(shopService.purchase(7, 3)).contains("余额不足");
        verify(shopItemMapper, never()).decreaseStock(anyInt());
        verify(creditAccountMapper, never()).updateBalance(anyInt(), anyLong());
    }

    // 测试库存不足时拒绝购买且不扣款
    @Test
    void shouldRejectPurchaseWhenStockInsufficient() {
        when(shopItemMapper.selectById(3)).thenReturn(shopItem(3, "勋章", ShopItemType.BADGE, 100L, 0L));
        when(userItemMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(creditAccountMapper.selectForUpdate(7)).thenReturn(creditAccount(7, 500L));
        when(shopItemMapper.decreaseStock(3)).thenReturn(0);

        assertThat(shopService.purchase(7, 3)).isEqualTo("库存不足");
        verify(creditAccountMapper, never()).updateBalance(anyInt(), anyLong());
        verify(shopOrderMapper, never()).insert(any(ShopOrder.class));
    }

    // 测试在售商品分页参数归一化
    @Test
    void shouldListOnSaleItemsWithNormalizedPaging() {
        ShopItemVO item = new ShopItemVO(3, "勋章", "test_badge", "描述", "badge", null, 100L, -1L, 0, 1);
        when(shopItemMapper.countOnSaleItems()).thenReturn(1L);
        when(shopItemMapper.selectOnSaleItems(0, 10)).thenReturn(List.of(item));

        PageEntity<ShopItemVO> page = shopService.listOnSaleItems(0, 0);

        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(item);
        verify(shopItemMapper).selectOnSaleItems(0, 10);
    }

    // 测试装备单装备类型时先卸下同类型旧装饰
    @Test
    void shouldUnequipSameTypeWhenEquippingSingleEquipItem() {
        UserItem userItem = userItem(11L, 7, 3);
        when(userItemMapper.selectById(11L)).thenReturn(userItem);
        when(shopItemMapper.selectById(3)).thenReturn(shopItem(3, "头像框", ShopItemType.AVATAR_FRAME, 100L, -1L));

        String result = shopService.updateEquipment(7, 11L, true);

        assertThat(result).isNull();
        verify(userItemMapper).unequipByType(7, "avatar_frame");
        ArgumentCaptor<UserItem> captor = ArgumentCaptor.forClass(UserItem.class);
        verify(userItemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsEquipped()).isTrue();
    }

    // 测试装备勋章时先卸下同类型旧勋章（勋章为单装备类型）
    @Test
    void shouldUnequipSameTypeWhenEquippingBadge() {
        UserItem userItem = userItem(11L, 7, 3);
        when(userItemMapper.selectById(11L)).thenReturn(userItem);
        when(shopItemMapper.selectById(3)).thenReturn(shopItem(3, "勋章", ShopItemType.BADGE, 100L, -1L));

        assertThat(shopService.updateEquipment(7, 11L, true)).isNull();
        verify(userItemMapper).unequipByType(7, "badge");
    }

    // 测试卸下装饰
    @Test
    void shouldUnequipItem() {
        UserItem userItem = userItem(11L, 7, 3);
        userItem.setIsEquipped(true);
        when(userItemMapper.selectById(11L)).thenReturn(userItem);

        assertThat(shopService.updateEquipment(7, 11L, false)).isNull();
        ArgumentCaptor<UserItem> captor = ArgumentCaptor.forClass(UserItem.class);
        verify(userItemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsEquipped()).isFalse();
    }

    // 测试操作他人背包记录时拒绝
    @Test
    void shouldRejectEquipmentUpdateForOthersItem() {
        when(userItemMapper.selectById(11L)).thenReturn(userItem(11L, 8, 3));

        assertThat(shopService.updateEquipment(7, 11L, true)).isEqualTo("装饰不存在");
        verify(userItemMapper, never()).updateById(any(UserItem.class));
    }

    // 测试背包分页与公开装饰查询
    @Test
    void shouldListMyItemsAndEquippedDecorations() {
        UserItemVO vo = new UserItemVO(11L, 3, "勋章", "test_badge", "badge", 1, true, null, new Date());
        when(userItemMapper.countByAccountId(7)).thenReturn(1L);
        when(userItemMapper.selectByAccountId(7, 0, 10)).thenReturn(List.of(vo));
        when(userItemMapper.selectEquippedByAccountId(7)).thenReturn(List.of(vo));

        PageEntity<UserItemVO> page = shopService.listMyItems(7, 1, 10);
        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(vo);
        assertThat(shopService.listEquippedDecorations(7)).containsExactly(vo);
        assertThat(shopService.listMyItems(null, 1, 10)).isNull();
        assertThat(shopService.listEquippedDecorations(null)).isNull();
    }

    private CreditTransaction captured(CreditTransactionMapper mapper) {
        ArgumentCaptor<CreditTransaction> captor = ArgumentCaptor.forClass(CreditTransaction.class);
        verify(mapper).insert(captor.capture());
        return captor.getValue();
    }

    private ShopOrder captured(ShopOrderMapper mapper) {
        ArgumentCaptor<ShopOrder> captor = ArgumentCaptor.forClass(ShopOrder.class);
        verify(mapper).insert(captor.capture());
        return captor.getValue();
    }

    private UserItem captured(UserItemMapper mapper) {
        ArgumentCaptor<UserItem> captor = ArgumentCaptor.forClass(UserItem.class);
        verify(mapper).insert(captor.capture());
        return captor.getValue();
    }

    private ShopItem shopItem(Integer itemId, String name, ShopItemType type, Long price, Long stock) {
        ShopItem item = new ShopItem();
        item.setItemId(itemId);
        item.setName(name);
        item.setItemType(type.getType());
        item.setPrice(price);
        item.setStock(stock);
        item.setPurchaseLimit(0);
        item.setStatus(ShopItemStatus.LISTED.getCode());
        item.setIsDeleted(false);
        return item;
    }

    private CreditAccount creditAccount(Integer accountId, Long balance) {
        return new CreditAccount(accountId, balance, new Date(), new Date());
    }

    private UserItem userItem(Long userItemId, Integer accountId, Integer itemId) {
        UserItem userItem = new UserItem();
        userItem.setUserItemId(userItemId);
        userItem.setAccountId(accountId);
        userItem.setItemId(itemId);
        userItem.setQuantity(1);
        userItem.setIsEquipped(false);
        return userItem;
    }
}
