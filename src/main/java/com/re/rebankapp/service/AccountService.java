package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.ChangePinRequest;
import com.re.rebankapp.dto.request.ForgotPinRequest;
import com.re.rebankapp.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    void changePin(Long userId, ChangePinRequest request);

    void forgotPin(Long userId, ForgotPinRequest request);

    List<AccountResponse> getMyAccounts(Long userId);

    // Mở thêm tài khoản thanh toán mới cho User đã được duyệt eKYC.
    AccountResponse createNewAccount(Long userId);
}
