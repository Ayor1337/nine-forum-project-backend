package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatBoardMessage {

    private Integer topicId;

    private String content;

}
