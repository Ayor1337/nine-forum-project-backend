package com.ayor.entity.dto;

import com.ayor.type.ShopItemStatus;
import com.ayor.type.ShopItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "商城商品参数")
public class ShopItemDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 64, message = "商品名称不能超过64个字符")
    @Schema(description = "商品名称（title 类型即头衔文本）")
    private String name;

    @NotBlank(message = "商品关键字不能为空")
    @Pattern(regexp = "^[a-z0-9][a-z0-9_-]{0,63}$", message = "商品关键字只能包含小写字母、数字、下划线和连字符，且不超过64个字符")
    @Schema(description = "商品关键字（唯一，前端素材映射用）")
    private String itemKey;

    @Size(max = 512, message = "商品描述不能超过512个字符")
    @Schema(description = "商品描述")
    private String description;

    @NotNull(message = "商品类型不能为空")
    @Schema(description = "商品类型")
    private ShopItemType itemType;

    @NotNull(message = "售价不能为空")
    @Min(value = 0, message = "售价不能为负数")
    @Schema(description = "售价（Credit）")
    private Long price;

    @NotNull(message = "库存不能为空")
    @Min(value = -1, message = "库存不能小于-1")
    @Schema(description = "库存，-1=不限量")
    private Long stock;

    @NotNull(message = "限购数量不能为空")
    @Min(value = 0, message = "限购数量不能为负数")
    @Schema(description = "每人限购数量，0=不限购")
    private Integer purchaseLimit;

    @NotNull(message = "商品状态不能为空")
    @Schema(description = "状态：LISTED=上架，DELISTED=下架")
    private ShopItemStatus status;
}
