package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ShopItemDTO;
import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.ShopOrderVO;
import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商城管理服务接口（管理端）
 *
 * 提供商品创建、更新、删除与商品/订单查询功能。
 */
public interface ShopService extends IService<ShopItem> {

    /**
     * 创建商品
     * @param dto 商品参数
     * @return 成功返回 null，失败返回错误消息
     */
    String createItem(ShopItemDTO dto);

    /**
     * 更新商品
     * @param itemId 商品ID
     * @param dto 商品参数
     * @return 成功返回 null，失败返回错误消息
     */
    String updateItem(Integer itemId, ShopItemDTO dto);

    /**
     * 删除商品（软删除）
     * @param itemId 商品ID
     * @return 成功返回 null，失败返回错误消息
     */
    String deleteItem(Integer itemId);

    /**
     * 分页查询商品，支持名称、类型、状态筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param name 商品名称（模糊，可选）
     * @param itemType 商品类型（可选）
     * @param status 商品状态（可选）
     * @return 商品分页数据
     */
    PageEntity<ShopItemVO> listItems(Integer pageNum,
                                     Integer pageSize,
                                     String name,
                                     ShopItemType itemType,
                                     ShopItemStatus status);

    /**
     * 分页查询购买记录，支持用户、商品筛选
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param accountId 买家账号ID（可选）
     * @param username 买家用户名（可选）
     * @param itemId 商品ID（可选）
     * @return 订单分页数据
     */
    PageEntity<ShopOrderVO> listOrders(Integer pageNum,
                                       Integer pageSize,
                                       Integer accountId,
                                       String username,
                                       Integer itemId);
}
