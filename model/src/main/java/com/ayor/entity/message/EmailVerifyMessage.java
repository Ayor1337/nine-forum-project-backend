package com.ayor.entity.message;

import com.ayor.type.EmailVerifyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerifyMessage {

    private String email;

    private String token;

    private EmailVerifyType type;

}
