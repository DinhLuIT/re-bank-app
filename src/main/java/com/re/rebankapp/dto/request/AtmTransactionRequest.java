package com.re.rebankapp.dto.request;

import com.re.rebankapp.validator.TransactionPin;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmTransactionRequest {

    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1000.0", message = "Số tiền giao dịch tối thiểu là 1,000 VND")
    private BigDecimal amount;

    @NotBlank(message = "Mã PIN không được để trống")
    @TransactionPin
    private String transactionPin;
}
