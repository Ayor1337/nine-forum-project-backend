package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ShopItemDTO;
import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.ShopOrderVO;
import com.ayor.mapper.ShopItemMapper;
import com.ayor.mapper.ShopOrderMapper;
import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopServiceImplTest {

    @Mock
    private ShopItemMapper shopItemMapper;

    @Mock
    private ShopOrderMapper shopOrderMapper;

    private ShopServiceImpl shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopServiceImpl(shopOrderMapper);
        ReflectionTestUtils.setField(shopService, "baseMapper", shopItemMapper);
    }

    // 测试创建商品时字段映射正确
    @Test
    void shouldCreateItemWithMappedFields() {
        when(shopItemMapper.countByItemKey("star_track_frame", null)).thenReturn(0L);
        String result = shopService.createItem(itemDto());

        assertThat(result).isNull();
        ArgumentCaptor<ShopItem> captor = ArgumentCaptor.forClass(ShopItem.class);
        verify(shopItemMapper).insert(captor.capture());
        ShopItem item = captor.getValue();
        assertThat(item.getName()).isEqualTo("头像框·星轨");
        assertThat(item.getItemKey()).isEqualTo("star_track_frame");
        assertThat(item.getItemType()).isEqualTo("avatar_frame");
        assertThat(item.getPrice()).isEqualTo(200L);
        assertThat(item.getStock()).isEqualTo(100L);
        assertThat(item.getPurchaseLimit()).isEqualTo(0);
        assertThat(item.getStatus()).isEqualTo(ShopItemStatus.LISTED.getCode());
        assertThat(item.getIsDeleted()).isFalse();
    }

    // 测试创建商品关键字重复时拒绝
    @Test
    void shouldRejectCreateWhenItemKeyDuplicated() {
        when(shopItemMapper.countByItemKey("star_track_frame", null)).thenReturn(1L);

        assertThat(shopService.createItem(itemDto())).isEqualTo("商品关键字已存在");
        verify(shopItemMapper, never()).insert(any(ShopItem.class));
    }

    // 测试创建商品参数缺失时拒绝
    @Test
    void shouldRejectCreateWhenParamsInvalid() {
        assertThat(shopService.createItem(null)).isEqualTo("参数错误");
        ShopItemDTO dto = itemDto();
        dto.setName("  ");
        assertThat(shopService.createItem(dto)).isEqualTo("参数错误");
        verify(shopItemMapper, never()).insert(any(ShopItem.class));
    }

    // 测试更新不存在的商品时拒绝
    @Test
    void shouldRejectUpdateWhenItemMissingOrDeleted() {
        when(shopItemMapper.selectById(3)).thenReturn(null);
        assertThat(shopService.updateItem(3, itemDto())).isEqualTo("商品不存在");

        ShopItem deleted = existingItem();
        deleted.setIsDeleted(true);
        when(shopItemMapper.selectById(4)).thenReturn(deleted);
        assertThat(shopService.updateItem(4, itemDto())).isEqualTo("商品不存在");

        verify(shopItemMapper, never()).updateById(any(ShopItem.class));
    }

    // 测试更新商品成功
    @Test
    void shouldUpdateItem() {
        when(shopItemMapper.selectById(3)).thenReturn(existingItem());
        when(shopItemMapper.countByItemKey("star_track_frame", 3)).thenReturn(0L);

        String result = shopService.updateItem(3, itemDto());

        assertThat(result).isNull();
        ArgumentCaptor<ShopItem> captor = ArgumentCaptor.forClass(ShopItem.class);
        verify(shopItemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("头像框·星轨");
        assertThat(captor.getValue().getItemKey()).isEqualTo("star_track_frame");
        assertThat(captor.getValue().getStatus()).isEqualTo(ShopItemStatus.LISTED.getCode());
    }

    // 测试更新商品关键字与其他商品重复时拒绝
    @Test
    void shouldRejectUpdateWhenItemKeyDuplicated() {
        when(shopItemMapper.selectById(3)).thenReturn(existingItem());
        when(shopItemMapper.countByItemKey("star_track_frame", 3)).thenReturn(1L);

        assertThat(shopService.updateItem(3, itemDto())).isEqualTo("商品关键字已存在");
        verify(shopItemMapper, never()).updateById(any(ShopItem.class));
    }

    // 测试删除商品为软删除
    @Test
    void shouldSoftDeleteItem() {
        when(shopItemMapper.selectById(3)).thenReturn(existingItem());

        assertThat(shopService.deleteItem(3)).isNull();
        ArgumentCaptor<ShopItem> captor = ArgumentCaptor.forClass(ShopItem.class);
        verify(shopItemMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsDeleted()).isTrue();

        assertThat(shopService.deleteItem(null)).isEqualTo("参数错误");
    }

    // 测试删除不存在的商品时拒绝
    @Test
    void shouldRejectDeleteWhenItemMissing() {
        when(shopItemMapper.selectById(3)).thenReturn(null);

        assertThat(shopService.deleteItem(3)).isEqualTo("商品不存在");
        verify(shopItemMapper, never()).updateById(any(ShopItem.class));
    }

    // 测试商品列表筛选与分页参数归一化
    @Test
    void shouldListItemsWithFilters() {
        ShopItemVO vo = new ShopItemVO(3, "头像框·星轨", "star_track_frame", "描述", "avatar_frame", 200L, 100L, 0, 1);
        when(shopItemMapper.countItems("头像框", "avatar_frame", 1)).thenReturn(1L);
        when(shopItemMapper.selectItems(0, 10, "头像框", "avatar_frame", 1)).thenReturn(List.of(vo));

        PageEntity<ShopItemVO> page = shopService.listItems(
                0, 0, " 头像框 ", ShopItemType.AVATAR_FRAME, ShopItemStatus.LISTED);

        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(vo);
        verify(shopItemMapper).selectItems(0, 10, "头像框", "avatar_frame", 1);
    }

    // 测试订单列表筛选与空白参数归一化
    @Test
    void shouldListOrdersWithFilters() {
        ShopOrderVO vo = new ShopOrderVO(9L, 7, "ayor", "阿尧", 3, "头像框·星轨", 200L, 1, 1, new Date());
        when(shopOrderMapper.countOrders(7, "ayor", 3)).thenReturn(1L);
        when(shopOrderMapper.selectOrders(0, 10, 7, "ayor", 3)).thenReturn(List.of(vo));

        PageEntity<ShopOrderVO> page = shopService.listOrders(1, 10, 7, "ayor", 3);

        assertThat(page.getTotalSize()).isEqualTo(1L);
        assertThat(page.getData()).containsExactly(vo);

        when(shopOrderMapper.countOrders(null, null, null)).thenReturn(0L);
        when(shopOrderMapper.selectOrders(0, 10, null, null, null)).thenReturn(List.of());
        PageEntity<ShopOrderVO> empty = shopService.listOrders(null, null, null, "  ", null);
        assertThat(empty.getTotalSize()).isEqualTo(0L);
    }

    private ShopItemDTO itemDto() {
        ShopItemDTO dto = new ShopItemDTO();
        dto.setName("头像框·星轨");
        dto.setItemKey("star_track_frame");
        dto.setDescription("环绕星轨的头像框");
        dto.setItemType(ShopItemType.AVATAR_FRAME);
        dto.setPrice(200L);
        dto.setStock(100L);
        dto.setPurchaseLimit(0);
        dto.setStatus(ShopItemStatus.LISTED);
        return dto;
    }

    private ShopItem existingItem() {
        ShopItem item = new ShopItem();
        item.setItemId(3);
        item.setName("旧商品");
        item.setItemKey("old_badge");
        item.setItemType("badge");
        item.setPrice(100L);
        item.setStock(-1L);
        item.setPurchaseLimit(0);
        item.setStatus(ShopItemStatus.LISTED.getCode());
        item.setIsDeleted(false);
        return item;
    }
}
