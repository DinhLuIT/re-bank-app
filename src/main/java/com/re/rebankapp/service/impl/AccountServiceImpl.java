package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.ChangePinRequest;
import com.re.rebankapp.dto.request.ForgotPinRequest;
import com.re.rebankapp.entity.Account;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.AccountRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void changePin(Long userId, ChangePinRequest request) {
        Account account = getAccountByUserId(userId);

        // Kiểm tra mã PIN hiện tại có đúng không
        if (!passwordEncoder.matches(request.getOldPin(), account.getTransactionPin())) {
            throw new AppException(ResponseCode.INVALID_OLD_PIN);
        }

        // Kiểm tra mã PIN mới và xác nhận mã PIN có khớp không
        validateNewPin(request.getNewPin(), request.getConfirmNewPin());

        // Kiểm tra mã PIN mới không được trùng với mã PIN hiện tại
        if (passwordEncoder.matches(request.getNewPin(), account.getTransactionPin())) {
            throw new AppException(ResponseCode.NEW_PIN_SAME_AS_OLD);
        }

        // Mã hóa và lưu mã PIN mới
        account.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        accountRepository.save(account);

        log.info("Đổi mã PIN thành công cho User có tài khoản [{}]", account.getAccountNumber());
    }

    @Override
    @Transactional
    public void forgotPin(Long userId, ForgotPinRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        // Xác thực mật khẩu đăng nhập (thay thế OTP ở giai đoạn này)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ResponseCode.INVALID_PASSWORD_FOR_PIN_RESET);
        }

        Account account = getAccountByUserId(userId);

        // Kiểm tra mã PIN mới và xác nhận mã PIN có khớp không
        validateNewPin(request.getNewPin(), request.getConfirmNewPin());

        // Mã hóa và lưu mã PIN mới
        account.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        accountRepository.save(account);

        log.info("Đặt lại mã PIN thành công (Quên mã PIN) cho User [{}]", user.getUsername());
    }

    /**
     * Tìm tài khoản ngân hàng theo userId.
     * Nếu chưa có tài khoản (chưa được duyệt eKYC) thì ném lỗi.
     */
    private Account getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND));
    }

    /**
     * Kiểm tra mã PIN mới và xác nhận mã PIN có khớp nhau không.
     */
    private void validateNewPin(String newPin, String confirmNewPin) {
        if (!newPin.equals(confirmNewPin)) {
            throw new AppException(ResponseCode.PIN_CONFIRM_MISMATCH);
        }
    }
}
