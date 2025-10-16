package com.ayor.entity.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagUpdateDTO {

    private Integer tagId;

    private Integer topicId;

    private Integer threadId;

}
