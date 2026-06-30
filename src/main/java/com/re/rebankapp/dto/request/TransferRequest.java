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
public class TransferRequest {

    @NotNull(message = "Mã tài khoản nguồn không được để trống")
    private Long sourceAccountId;
    
    @NotBlank(message = "Số tài khoản nhận không được để trống")
    private String targetAccountNumber;
    
    @NotNull(message = "Số tiền chuyển không được để trống")
    @DecimalMin(value = "1000.0", message = "Số tiền chuyển tối thiểu là 1,000 VND")
    private BigDecimal amount;
    
    private String description;
    
    @NotBlank(message = "Mã PIN không được để trống")
    @TransactionPin
    private String transactionPin;
}
