package com.ayor.entity.wsMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatBoardResultMessage<T> extends ResultMessage<T>{

    private Integer topicId;

}
