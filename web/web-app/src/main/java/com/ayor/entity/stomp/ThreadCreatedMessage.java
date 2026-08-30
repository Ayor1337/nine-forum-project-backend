package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadCreatedMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 120L;

    private Integer topicId;

    private Integer threadId;

    private Integer increment;

    private Date createTime;
}
