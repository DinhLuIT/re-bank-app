package com.re.rebankapp.dto.response;

import com.re.rebankapp.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Boolean isKyc;
    private RoleName roleName;
    private LocalDateTime createdAt;
}
