package com.re.rebankapp.controller.user;

import com.re.rebankapp.dto.request.ChangePinRequest;
import com.re.rebankapp.dto.request.ForgotPinRequest;
import com.re.rebankapp.dto.response.AccountResponse;
import com.re.rebankapp.dto.response.ApiResponse;
import com.re.rebankapp.security.UserDetailsImpl;
import com.re.rebankapp.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@PreAuthorize("hasAuthority('CUSTOMER')")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ApiResponse<List<AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<AccountResponse> accounts = accountService.getMyAccounts(userDetails.getId());
        return ApiResponse.success(accounts);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createNewAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        AccountResponse newAccount = accountService.createNewAccount(userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(newAccount));
    }

    @PutMapping("/pin/change")
    public ResponseEntity<ApiResponse<String>> changePin(
            @Valid @RequestBody ChangePinRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        accountService.changePin(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mã PIN giao dịch thành công"));
    }

    @PutMapping("/pin/forgot")
    public ResponseEntity<ApiResponse<String>> forgotPin(
            @Valid @RequestBody ForgotPinRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        accountService.forgotPin(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đặt lại mã PIN giao dịch thành công"));
    }
}
