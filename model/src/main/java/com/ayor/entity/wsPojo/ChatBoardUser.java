package com.ayor.entity.wsPojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatBoardUser {

    private String fromUser;

    private Integer topicId;

}
