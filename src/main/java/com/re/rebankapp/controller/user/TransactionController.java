package com.re.rebankapp.controller.user;

import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.PageMeta;
import com.re.rebankapp.dto.response.StatementResponse;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/accounts/balance")
    public ApiResponse<Map<String, BigDecimal>> getBalance() {
        BigDecimal balance = transactionService.getBalance();
        return ApiResponse.success(Map.of("balance", balance));
    }

    @PostMapping("/transactions/transfer")
    public ApiResponse<Void> transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.transfer(request);
        return ApiResponse.success();
    }

    @GetMapping("/transactions/statement")
    public ApiResponse<Object> getStatement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StatementResponse> statements = transactionService.getStatement(pageable);

        PageMeta meta = PageMeta.builder()
                .currentPage(statements.getNumber())
                .pageSize(statements.getSize())
                .totalPage(statements.getTotalPages())
                .totalElements(statements.getTotalElements())
                .build();

        return ApiResponse.success(statements.getContent(), meta);
    }
}
