package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {

    @NotNull
    private String tag;

    @NotNull
    private Integer topicId;

}
