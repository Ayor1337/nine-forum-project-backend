package com.ayor.entity.wsMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultMessage<T>  {

    private Boolean isSystem;

    private String fromUser;

    private String toUser;

    private T message;

}
