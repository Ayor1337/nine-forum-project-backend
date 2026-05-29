package com.ayor.entity.dto;

import com.ayor.entity.Base64Upload;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileDTO {

    @Size(min = 3, max = 20)
    private String nickname;

    @Size(min = 6, max = 50)
    private String bio;

    @Size(max = 100)
    private String location;

    private Date birthday;

    @Size(max = 255)
    private String website;

    private Base64Upload avatar;

}
