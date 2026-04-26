package com.ayor.entity.app.dto;

import com.ayor.entity.Base64Upload;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountProfileDTO {

    @Size(min = 3, max = 20)
    private String nickname;

    @Size(min = 6, max = 50)
    private String bio;

    private Base64Upload avatar;

}
