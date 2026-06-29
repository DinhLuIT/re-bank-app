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
public class ForgotPinRequest {

    @NotBlank(message = "Mật khẩu đăng nhập không được để trống")
    private String password;

    @NotBlank(message = "Mã PIN mới không được để trống")
    @TransactionPin
    private String newPin;

    @NotBlank(message = "Xác nhận mã PIN mới không được để trống")
    @TransactionPin
    private String confirmNewPin;
}
