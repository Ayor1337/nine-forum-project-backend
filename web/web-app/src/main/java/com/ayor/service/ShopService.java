package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.ShopItem;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.UserItemVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商城服务接口（用户端）
 *
 * 提供在售商品浏览、购买、背包查询与装饰装备功能。
 */
public interface ShopService extends IService<ShopItem> {

    /**
     * 分页查询在售商品
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 商品分页数据
     */
    PageEntity<ShopItemVO> listOnSaleItems(Integer pageNum, Integer pageSize);

    /**
     * 购买商品（扣减 Credit，同事务写入流水、订单与背包）
     * @param accountId 用户ID
     * @param itemId 商品ID
     * @return 成功返回 null，失败返回错误消息
     */
    String purchase(Integer accountId, Integer itemId);

    /**
     * 分页查询用户背包
     * @param accountId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 背包分页数据
     */
    PageEntity<UserItemVO> listMyItems(Integer accountId, Integer pageNum, Integer pageSize);

    /**
     * 装备或卸下背包中的装饰
     * @param accountId 用户ID
     * @param userItemId 背包记录ID
     * @param equipped true=装备，false=卸下
     * @return 成功返回 null，失败返回错误消息
     */
    String updateEquipment(Integer accountId, Long userItemId, Boolean equipped);

    /**
     * 查询用户当前装备的装饰（个人页展示）
     * @param accountId 用户ID
     * @return 已装备装饰列表
     */
    List<UserItemVO> listEquippedDecorations(Integer accountId);
}
