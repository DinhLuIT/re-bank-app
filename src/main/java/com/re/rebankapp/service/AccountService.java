package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.ChangePinRequest;
import com.re.rebankapp.dto.request.ForgotPinRequest;

public interface AccountService {

    void changePin(Long userId, ChangePinRequest request);

    void forgotPin(Long userId, ForgotPinRequest request);
}
