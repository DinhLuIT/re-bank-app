package com.re.rebankapp.service.impl;

import com.re.rebankapp.dto.request.AtmTransactionRequest;
import com.re.rebankapp.dto.request.InterbankTransferRequest;
import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.dto.response.StatementResponse;
import com.re.rebankapp.entity.Account;
import com.re.rebankapp.entity.Transaction;
import com.re.rebankapp.enums.TransactionType;
import com.re.rebankapp.exception.AppException;
import com.re.rebankapp.exception.ResponseCode;
import com.re.rebankapp.repository.AccountRepository;
import com.re.rebankapp.repository.TransactionRepository;
import com.re.rebankapp.security.UserDetailsImpl;
import com.re.rebankapp.service.InterbankService;
import com.re.rebankapp.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final InterbankService interbankService;

    @Override
    public BigDecimal getBalance(Long accountId) {
        Account myAccount = getOwnedAccount(accountId);
        return myAccount.getBalance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(TransferRequest request) {
        // 1. Xác thực quyền sở hữu tài khoản nguồn (Chống IDOR)
        Account myAccount = getOwnedAccount(request.getSourceAccountId());

        // 2. Kiểm tra không được chuyển cho chính mình
        if (myAccount.getAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new AppException(ResponseCode.SAME_ACCOUNT_TRANSFER);
        }

        // 3. Kiểm tra mã PIN (Phải nhập đúng PIN và ĐÃ đổi PIN lần đầu)
        if (!passwordEncoder.matches(request.getTransactionPin(), myAccount.getTransactionPin())) {
            throw new AppException(ResponseCode.INVALID_OLD_PIN);
        }

        if (!myAccount.getIsPinChanged()) {
            throw new AppException(ResponseCode.DEFAULT_PIN_NOT_ALLOWED);
        }

        // 4. Cơ chế Pessimistic Locking để chống Race Condition (Double-spending)
        // Phải lock theo thứ tự từ điển của số tài khoản để chống Deadlock
        Account lockedSender;
        Account lockedReceiver;

        String senderAccNo = myAccount.getAccountNumber();
        String receiverAccNo = request.getTargetAccountNumber();

        if (senderAccNo.compareTo(receiverAccNo) < 0) {
            lockedSender = accountRepository.findByAccountNumberForUpdate(senderAccNo)
                    .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND));
            lockedReceiver = accountRepository.findByAccountNumberForUpdate(receiverAccNo)
                    .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND));
        } else {
            lockedReceiver = accountRepository.findByAccountNumberForUpdate(receiverAccNo)
                    .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND));
            lockedSender = accountRepository.findByAccountNumberForUpdate(senderAccNo)
                    .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND));
        }

        // 5. Kiểm tra số dư của người gửi
        if (lockedSender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ResponseCode.INSUFFICIENT_BALANCE);
        }

        // 6. Kiểm tra hạn mức giao dịch trong ngày (Daily Limit)
        BigDecimal dailySpent = transactionRepository.sumDailyOutflow(lockedSender.getId());
        BigDecimal totalAfterTransfer = dailySpent.add(request.getAmount());
        if (totalAfterTransfer.compareTo(lockedSender.getDailyLimit()) > 0) {
            throw new AppException(ResponseCode.DAILY_LIMIT_EXCEEDED);
        }

        // 7. Thực hiện trừ và cộng tiền
        lockedSender.setBalance(lockedSender.getBalance().subtract(request.getAmount()));
        lockedReceiver.setBalance(lockedReceiver.getBalance().add(request.getAmount()));

        accountRepository.save(lockedSender);
        accountRepository.save(lockedReceiver);

        // 8. Lưu lịch sử giao dịch
        String txnCode = "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transaction transaction = Transaction.builder()
                .transactionCode(txnCode)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .fromAccount(lockedSender)
                .toAccount(lockedReceiver)
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void atmDeposit(AtmTransactionRequest request) {
        Account account = getOwnedAccountForUpdate(request.getAccountNumber());

        // Kiểm tra mã PIN (Phải nhập đúng PIN và ĐÃ đổi PIN lần đầu)
        if (!passwordEncoder.matches(request.getTransactionPin(), account.getTransactionPin())) {
            throw new AppException(ResponseCode.INVALID_OLD_PIN);
        }

        if (!account.getIsPinChanged()) {
            throw new AppException(ResponseCode.DEFAULT_PIN_NOT_ALLOWED);
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        String txnCode = "ATM_DEP_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transaction transaction = Transaction.builder()
                .transactionCode(txnCode)
                .amount(request.getAmount())
                .description("Nạp tiền tại ATM")
                .status("SUCCESS")
                .fromAccount(null)
                .toAccount(account)
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void atmWithdraw(AtmTransactionRequest request) {
        Account account = getOwnedAccountForUpdate(request.getAccountNumber());

        // Kiểm tra mã PIN (Phải nhập đúng PIN và ĐÃ đổi PIN lần đầu)
        if (!passwordEncoder.matches(request.getTransactionPin(), account.getTransactionPin())) {
            throw new AppException(ResponseCode.INVALID_OLD_PIN);
        }

        if (!account.getIsPinChanged()) {
            throw new AppException(ResponseCode.DEFAULT_PIN_NOT_ALLOWED);
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ResponseCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal dailySpent = transactionRepository.sumDailyOutflow(account.getId());
        if (dailySpent.add(request.getAmount()).compareTo(account.getDailyLimit()) > 0) {
            throw new AppException(ResponseCode.DAILY_LIMIT_EXCEEDED);
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        String txnCode = "ATM_WDL_" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transaction transaction = Transaction.builder()
                .transactionCode(txnCode)
                .amount(request.getAmount())
                .description("Rút tiền tại ATM")
                .status("SUCCESS")
                .fromAccount(account)
                .toAccount(null)
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    public Page<StatementResponse> getStatement(Long accountId, Pageable pageable) {
        Account myAccount = getOwnedAccount(accountId);
        Page<StatementResponse> statements = transactionRepository.findAllByAccountId(myAccount.getId(), pageable);

        // Gắn nhãn động CREDIT/DEBIT
        statements.forEach(statement -> {
            if (myAccount.getId().equals(statement.getFromAccountId())) {
                statement.setType(TransactionType.DEBIT); // Trừ tiền
            } else if (myAccount.getId().equals(statement.getToAccountId())) {
                statement.setType(TransactionType.CREDIT); // Cộng tiền
            }
        });

        return statements;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void interbankTransfer(InterbankTransferRequest request) {
        // 1. Lock tài khoản người gửi
        String senderAccNo = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND))
                .getAccountNumber();
        Account lockedSender = getOwnedAccountForUpdate(senderAccNo);

        // 2. Kiểm tra mã PIN (Phải nhập đúng PIN và ĐÃ đổi PIN lần đầu)
        if (!passwordEncoder.matches(request.getTransactionPin(), lockedSender.getTransactionPin())) {
            throw new AppException(ResponseCode.INVALID_OLD_PIN);
        }

        if (!lockedSender.getIsPinChanged()) {
            throw new AppException(ResponseCode.DEFAULT_PIN_NOT_ALLOWED);
        }

        // 3. Kiểm tra số dư và hạn mức
        if (lockedSender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ResponseCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal dailySpent = transactionRepository.sumDailyOutflow(lockedSender.getId());
        if (dailySpent.add(request.getAmount()).compareTo(lockedSender.getDailyLimit()) > 0) {
            throw new AppException(ResponseCode.DAILY_LIMIT_EXCEEDED);
        }

        // 4. Gọi dịch vụ Napas
        boolean isSuccess = interbankService.transferToExternalBank(
                lockedSender.getAccountNumber(),
                request.getTargetAccountNumber(),
                request.getBankName(),
                request.getAmount()
        );

        if (!isSuccess) {
            throw new AppException(ResponseCode.BAD_REQUEST);
        }

        // 5. Trừ tiền và lưu giao dịch
        lockedSender.setBalance(lockedSender.getBalance().subtract(request.getAmount()));
        accountRepository.save(lockedSender);

        String txnCode = "IBFT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Transaction transaction = Transaction.builder()
                .transactionCode(txnCode)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status("SUCCESS")
                .fromAccount(lockedSender)
                .toAccount(null)
                .externalAccountNumber(request.getTargetAccountNumber())
                .externalBankName(request.getBankName())
                .build();

        transactionRepository.save(transaction);
    }

    /**
     * Lấy tài khoản và đồng thời kiểm tra quyền sở hữu (chống IDOR).
     * Đảm bảo accountId truyền vào ĐÚNG LÀ thuộc về User đang đăng nhập.
     */
    private Account getOwnedAccount(Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, getCurrentUserId())
                .orElseThrow(() -> new AppException(ResponseCode.ACCOUNT_NOT_OWNED));
    }

    /**
     * Đảm bảo tài khoản này thuộc về User đang đăng nhập.
     */
    private Account getOwnedAccountForUpdate(String accountNumber) {
        return accountRepository.findByAccountNumberAndUserIdForUpdate(accountNumber, getCurrentUserId())
                .orElseThrow(() -> new AppException(ResponseCode.ACCOUNT_NOT_OWNED));
    }

    private Long getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }
}
