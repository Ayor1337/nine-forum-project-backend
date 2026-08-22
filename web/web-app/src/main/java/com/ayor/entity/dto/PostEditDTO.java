package com.ayor.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostEditDTO {

    @NotNull(message = "内容不能为空")
    private String content;

    private List<String> imageUrls;

    public PostEditDTO(String content) {
        this.content = content;
    }

}
