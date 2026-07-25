package com.ayor.controller;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ShopItemDTO;
import com.ayor.entity.vo.ShopItemVO;
import com.ayor.entity.vo.ShopOrderVO;
import com.ayor.result.Result;
import com.ayor.service.ShopService;
import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "商城管理", description = "后台商品管理与购买记录查询接口")
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/items")
    @Operation(summary = "创建商品")
    public Result<Void> createItem(@RequestBody @Valid ShopItemDTO dto) {
        return Result.messageHandler(() -> shopService.createItem(dto));
    }

    @PutMapping("/items/{item_id}")
    @Operation(summary = "更新商品")
    public Result<Void> updateItem(
            @Parameter(description = "商品ID") @PathVariable("item_id") Integer itemId,
            @RequestBody @Valid ShopItemDTO dto) {
        return Result.messageHandler(() -> shopService.updateItem(itemId, dto));
    }

    @DeleteMapping("/items/{item_id}")
    @Operation(summary = "删除商品（软删除）")
    public Result<Void> deleteItem(
            @Parameter(description = "商品ID") @PathVariable("item_id") Integer itemId) {
        return Result.messageHandler(() -> shopService.deleteItem(itemId));
    }

    @GetMapping("/items")
    @Operation(summary = "分页查询商品")
    public Result<PageEntity<ShopItemVO>> listItems(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "商品名称（模糊）") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "商品类型") @RequestParam(value = "item_type", required = false) ShopItemType itemType,
            @Parameter(description = "商品状态") @RequestParam(value = "status", required = false) ShopItemStatus status) {
        return Result.dataMessageHandler(
                () -> shopService.listItems(pageNum, pageSize, name, itemType, status),
                "获取商品列表失败");
    }

    @GetMapping("/orders")
    @Operation(summary = "分页查询购买记录")
    public Result<PageEntity<ShopOrderVO>> listOrders(
            @Parameter(description = "页码") @RequestParam(value = "page_num", defaultValue = "1", required = false) Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(value = "page_size", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "买家账号ID") @RequestParam(value = "account_id", required = false) Integer accountId,
            @Parameter(description = "买家用户名") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "商品ID") @RequestParam(value = "item_id", required = false) Integer itemId) {
        return Result.dataMessageHandler(
                () -> shopService.listOrders(pageNum, pageSize, accountId, username, itemId),
                "获取购买记录失败");
    }
}
