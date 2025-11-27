package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountDTO {

    @Size(min = 4, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;

    @Size(min = 6, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String password;

    @Size(min = 3, max = 20)
    private String nickname;

    @NotNull
    private String token;

}
