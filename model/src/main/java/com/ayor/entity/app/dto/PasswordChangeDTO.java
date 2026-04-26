package com.ayor.entity.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeDTO {

    @NotNull
    private String oldPassword;

    @Size(min = 6, max = 16)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String newPassword;

}
