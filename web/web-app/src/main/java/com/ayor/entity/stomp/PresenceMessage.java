package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceMessage {

    private Integer userId;

    private Boolean online;

    private Date time;
}
