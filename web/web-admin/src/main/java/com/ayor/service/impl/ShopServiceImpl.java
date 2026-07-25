package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ShopItemDTO;
import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.ShopOrderVO;
import com.ayor.mapper.ShopItemMapper;
import com.ayor.mapper.ShopOrderMapper;
import com.ayor.service.ShopService;
import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商城管理服务实现（管理端）
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopItemMapper, ShopItem> implements ShopService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ShopOrderMapper shopOrderMapper;

    @Override
    public String createItem(ShopItemDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())
                || dto.getItemType() == null || dto.getPrice() == null
                || dto.getStock() == null || dto.getPurchaseLimit() == null || dto.getStatus() == null) {
            return "参数错误";
        }
        ShopItem item = new ShopItem();
        applyDto(item, dto);
        item.setIsDeleted(false);
        this.save(item);
        return null;
    }

    @Override
    public String updateItem(Integer itemId, ShopItemDTO dto) {
        if (itemId == null || dto == null) {
            return "参数错误";
        }
        ShopItem item = this.getById(itemId);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            return "商品不存在";
        }
        applyDto(item, dto);
        this.updateById(item);
        return null;
    }

    @Override
    public String deleteItem(Integer itemId) {
        if (itemId == null) {
            return "参数错误";
        }
        ShopItem item = this.getById(itemId);
        if (item == null || Boolean.TRUE.equals(item.getIsDeleted())) {
            return "商品不存在";
        }
        item.setIsDeleted(true);
        this.updateById(item);
        return null;
    }

    @Override
    public PageEntity<ShopItemVO> listItems(Integer pageNum,
                                            Integer pageSize,
                                            String name,
                                            ShopItemType itemType,
                                            ShopItemStatus status) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        String normalizedName = StringUtils.hasText(name) ? name.trim() : null;
        String itemTypeValue = itemType == null ? null : itemType.getType();
        Integer statusValue = status == null ? null : status.getCode();
        return new PageEntity<>(
                baseMapper.countItems(normalizedName, itemTypeValue, statusValue),
                baseMapper.selectItems(offset, normalizedPageSize, normalizedName, itemTypeValue, statusValue));
    }

    @Override
    public PageEntity<ShopOrderVO> listOrders(Integer pageNum,
                                              Integer pageSize,
                                              Integer accountId,
                                              String username,
                                              Integer itemId) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        String normalizedUsername = StringUtils.hasText(username) ? username : null;
        return new PageEntity<>(
                shopOrderMapper.countOrders(accountId, normalizedUsername, itemId),
                shopOrderMapper.selectOrders(offset, normalizedPageSize, accountId, normalizedUsername, itemId));
    }

    private void applyDto(ShopItem item, ShopItemDTO dto) {
        item.setName(dto.getName().trim());
        item.setDescription(dto.getDescription());
        item.setItemType(dto.getItemType().getType());
        item.setPrice(dto.getPrice());
        item.setStock(dto.getStock());
        item.setPurchaseLimit(dto.getPurchaseLimit());
        item.setStatus(dto.getStatus().getCode());
    }
}
