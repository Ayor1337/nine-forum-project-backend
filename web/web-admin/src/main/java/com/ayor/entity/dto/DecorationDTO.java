package com.ayor.entity.dto;

import com.ayor.type.ShopItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "装扮设计参数")
public class DecorationDTO {

    @NotBlank(message = "装扮名称不能为空")
    @Size(max = 64, message = "装扮名称不能超过64个字符")
    @Schema(description = "装扮名称（title 类型即头衔文本）")
    private String name;

    @NotBlank(message = "装扮关键字不能为空")
    @Pattern(regexp = "^[a-z0-9][a-z0-9_-]{0,63}$", message = "装扮关键字只能包含小写字母、数字、下划线和连字符，且不超过64个字符")
    @Schema(description = "装扮关键字（唯一）")
    private String decorationKey;

    @Size(max = 512, message = "装扮描述不能超过512个字符")
    @Schema(description = "装扮描述")
    private String description;

    @NotNull(message = "装扮类型不能为空")
    @Schema(description = "装扮类型")
    private ShopItemType type;

    @Schema(description = "草稿配置（结构化 JSON 文本，遵循渲染协议）")
    private String draftConfig;
}
