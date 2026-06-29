package com.re.rebankapp.dto.response;

import com.re.rebankapp.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycAdminResponse {

    private Long id;
    private String fullName;
    private String idNumber;
    private Status status;
    private LocalDateTime createdAt;
}
