package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.ChangePinRequest;
import com.re.rebankapp.dto.request.ForgotPinRequest;
import com.re.rebankapp.dto.response.AccountResponse;
import com.re.rebankapp.entity.Account;
import com.re.rebankapp.entity.User;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.AccountRepository;
import com.re.rebankapp.repository.UserRepository;
import com.re.rebankapp.mapper.AccountMapper;
import com.re.rebankapp.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT_PREFIX = "8868";
    private static final int ACCOUNT_SUFFIX_LENGTH = 9;
    private static final String DEFAULT_TRANSACTION_PIN = "000000";
    private static final String DEFAULT_CURRENCY = "VND";
    private static final int MAX_ACCOUNTS_PER_USER = 5;

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void changePin(Long userId, ChangePinRequest request) {
        // Lấy tài khoản đầu tiên của User (Tính năng PIN áp dụng cho thẻ chính)
        List<Account> accounts = accountRepository.findAllByUserId(userId);
        if (accounts.isEmpty()) {
            throw new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND);
        }
        Account account = accounts.get(0);

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

        // Mã hóa và đồng bộ mã PIN mới cho TẤT CẢ tài khoản của User
        String encodedNewPin = passwordEncoder.encode(request.getNewPin());
        accounts.forEach(acc -> acc.setTransactionPin(encodedNewPin));
        accountRepository.saveAll(accounts);

        log.info("Đổi mã PIN thành công cho User có tài khoản [{}] (Đồng bộ {} tài khoản)", account.getAccountNumber(), accounts.size());
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

        List<Account> accounts = accountRepository.findAllByUserId(userId);
        if (accounts.isEmpty()) {
            throw new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND);
        }

        // Kiểm tra mã PIN mới và xác nhận mã PIN có khớp không
        validateNewPin(request.getNewPin(), request.getConfirmNewPin());

        // Mã hóa và đồng bộ mã PIN mới cho TẤT CẢ tài khoản của User
        String encodedNewPin = passwordEncoder.encode(request.getNewPin());
        accounts.forEach(acc -> acc.setTransactionPin(encodedNewPin));
        accountRepository.saveAll(accounts);

        log.info("Đặt lại mã PIN thành công (Quên mã PIN) cho User [{}] (Đồng bộ {} tài khoản)", user.getUsername(), accounts.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(Long userId) {
        List<Account> accounts = accountRepository.findAllByUserId(userId);
        if (accounts.isEmpty()) {
            throw new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND);
        }
        return accountMapper.toAccountResponseList(accounts);
    }

    @Override
    @Transactional
    public AccountResponse createNewAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResponseCode.USER_NOT_FOUND));

        // Kiểm tra User đã được duyệt eKYC chưa
        if (!user.getIsKyc()) {
            throw new AppException(ResponseCode.KYC_NOT_FOUND);
        }

        // Kiểm tra giới hạn số lượng tài khoản
        long currentAccountCount = accountRepository.countByUserId(userId);
        if (currentAccountCount >= MAX_ACCOUNTS_PER_USER) {
            throw new AppException(ResponseCode.MAX_ACCOUNTS_REACHED);
        }

        // Lấy mã PIN từ tài khoản chính (thẻ đầu tiên) để đồng bộ cho thẻ mới
        List<Account> existingAccounts = accountRepository.findAllByUserId(userId);
        String transactionPin;
        if (!existingAccounts.isEmpty()) {
            transactionPin = existingAccounts.get(0).getTransactionPin();
        } else {
            transactionPin = passwordEncoder.encode(DEFAULT_TRANSACTION_PIN);
        }

        Account newAccount = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .balance(BigDecimal.ZERO)
                .currency(DEFAULT_CURRENCY)
                .transactionPin(transactionPin)
                .user(user)
                .build();

        Account saved = accountRepository.save(newAccount);
        log.info("Mở thêm tài khoản phụ [{}] cho User [{}]. Tổng số thẻ: {}", saved.getAccountNumber(), user.getUsername(), currentAccountCount + 1);

        return accountMapper.toAccountResponse(saved);
    }

    /**
     * Kiểm tra mã PIN mới và xác nhận mã PIN có khớp nhau không.
     */
    private void validateNewPin(String newPin, String confirmNewPin) {
        if (!newPin.equals(confirmNewPin)) {
            throw new AppException(ResponseCode.PIN_CONFIRM_MISMATCH);
        }
    }

    /**
     * Sinh số tài khoản ngẫu nhiên duy nhất (Prefix 8868 + 9 chữ số).
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            StringBuilder sb = new StringBuilder(ACCOUNT_PREFIX);
            for (int i = 0; i < ACCOUNT_SUFFIX_LENGTH; i++) {
                sb.append(secureRandom.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }


}
