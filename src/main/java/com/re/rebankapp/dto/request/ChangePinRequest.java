package com.re.rebankapp.dto.request;

import com.re.rebankapp.validator.TransactionPin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePinRequest {

    @NotBlank(message = "Mã PIN hiện tại không được để trống")
    @TransactionPin
    private String oldPin;

    @NotBlank(message = "Mã PIN mới không được để trống")
    @TransactionPin
    private String newPin;

    @NotBlank(message = "Xác nhận mã PIN mới không được để trống")
    @TransactionPin
    private String confirmNewPin;
}
