package com.re.rebankapp.dto.response;

import com.re.rebankapp.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.re.rebankapp.enums.Gender;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KycProfileResponse {

    private Long id;
    private String idNumber;
    private String fullName;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String idCardFrontUrl;
    private Status status;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
}
