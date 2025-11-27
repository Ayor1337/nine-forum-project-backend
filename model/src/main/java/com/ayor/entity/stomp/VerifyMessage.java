package com.ayor.entity.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyMessage {

    private Boolean isVerified;

    private String token;
}
