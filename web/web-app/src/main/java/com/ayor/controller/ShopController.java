package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.DecorationEquipDTO;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.UserItemVO;
import com.ayor.result.Result;
import com.ayor.service.ShopService;
import com.ayor.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/shop")
@Tag(name = "商城", description = "商品浏览、购买与装饰装备接口")
public class ShopController {

    private final ShopService shopService;

    private final SecurityUtils security;

    /**
     * 分页查询在售商品。
     */
    @Operation(summary = "分页查询在售商品")
    @GetMapping("/items")
    public Result<PageEntity<ShopItemVO>> listOnSaleItems(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize) {
        return Result.dataMessageHandler(() -> shopService.listOnSaleItems(pageNum, pageSize), "获取商品列表失败");
    }

    /**
     * 购买商品。
     */
    @Operation(summary = "购买商品")
    @PostMapping("/items/{item_id}/purchases")
    public Result<Void> purchase(@Parameter(description = "商品 ID") @PathVariable(name = "item_id") Integer itemId) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> shopService.purchase(userId, itemId));
    }

    /**
     * 分页查询当前用户背包。
     */
    @Operation(summary = "分页查询当前用户背包")
    @GetMapping("/my-items")
    public Result<PageEntity<UserItemVO>> listMyItems(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize) {
        Integer userId = security.getSecurityUserId();
        return Result.dataMessageHandler(() -> shopService.listMyItems(userId, pageNum, pageSize), "获取背包失败");
    }

    /**
     * 装备或卸下背包中的装饰。
     */
    @Operation(summary = "装备或卸下装饰")
    @PutMapping("/my-items/{user_item_id}/equipment")
    public Result<Void> updateEquipment(
            @Parameter(description = "背包记录 ID") @PathVariable(name = "user_item_id") Long userItemId,
            @RequestBody @Valid DecorationEquipDTO dto) {
        Integer userId = security.getSecurityUserId();
        return Result.messageHandler(() -> shopService.updateEquipment(userId, userItemId, dto.getEquipped()));
    }

    /**
     * 查询指定用户当前装备的装饰。
     */
    @Operation(summary = "查询指定用户当前装备的装饰")
    @GetMapping("/users/{account_id}/decorations")
    public Result<List<UserItemVO>> listEquippedDecorations(
            @Parameter(description = "用户账号 ID") @PathVariable(name = "account_id") Integer accountId) {
        return Result.dataMessageHandler(() -> shopService.listEquippedDecorations(accountId), "获取装饰失败");
    }
}
