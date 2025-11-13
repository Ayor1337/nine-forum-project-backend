package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdateDTO {

    @NotNull
    private Integer tagId;

    @NotNull
    private Integer topicId;

    @NotNull
    private Integer threadId;

}
