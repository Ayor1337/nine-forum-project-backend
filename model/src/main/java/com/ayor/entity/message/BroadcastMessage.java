package com.ayor.entity.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "广播消息")
public class BroadcastMessage <T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private T message;


}
