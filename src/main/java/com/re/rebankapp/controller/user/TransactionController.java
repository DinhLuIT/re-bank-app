package com.re.rebankapp.controller.user;

import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.dto.response.PageMeta;
import com.re.rebankapp.dto.response.StatementResponse;
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

    /**
     * Vấn tin số dư cho một tài khoản cụ thể (Kiến trúc 1-N).
     * Bắt buộc truyền accountId để chỉ định rõ tài khoản nào cần tra cứu.
     */
    @GetMapping("/accounts/{accountId}/balance")
    public ApiResponse<Map<String, BigDecimal>> getBalance(@PathVariable Long accountId) {
        BigDecimal balance = transactionService.getBalance(accountId);
        return ApiResponse.success(Map.of("balance", balance));
    }

    /**
     * Chuyển tiền - sourceAccountId nằm bên trong body TransferRequest.
     */
    @PostMapping("/transactions/transfer")
    public ApiResponse<Void> transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.transfer(request);
        return ApiResponse.success();
    }

    /**
     * Sao kê
     * Bắt buộc truyền accountId để chỉ định rõ tài khoản nào cần xem sao kê.
     */
    @GetMapping("/accounts/{accountId}/statements")
    public ApiResponse<Object> getStatement(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<StatementResponse> statements = transactionService.getStatement(accountId, pageable);

        PageMeta meta = PageMeta.builder()
                .currentPage(statements.getNumber())
                .pageSize(statements.getSize())
                .totalPage(statements.getTotalPages())
                .totalElements(statements.getTotalElements())
                .build();

        return ApiResponse.success(statements.getContent(), meta);
    }
}
