package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationMessageDTO {

    @NotNull
    private Integer conversationId;

    @NotNull
    private String content;

    @NotNull
    private Integer toUserId;

}
