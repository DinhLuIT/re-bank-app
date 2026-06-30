package com.re.rebankapp.service.impl;

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

    @Override
    public BigDecimal getBalance() {
        Account myAccount = getMyAccount();
        return myAccount.getBalance();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transfer(TransferRequest request) {
        Account myAccount = getMyAccount();
        
        // 1. Kiểm tra không được chuyển cho chính mình
        if (myAccount.getAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new AppException(ResponseCode.SAME_ACCOUNT_TRANSFER);
        }

        // 2. Xác thực mã PIN giao dịch
        if (!passwordEncoder.matches(request.getTransactionPin(), myAccount.getTransactionPin())) {
            throw new AppException(ResponseCode.INVALID_OLD_PIN);
        }

        // 3. Cơ chế Pessimistic Locking để chống Race Condition (Double-spending)
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

        // 4. Kiểm tra số dư của người gửi
        if (lockedSender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ResponseCode.INSUFFICIENT_BALANCE);
        }

        // 5. Thực hiện trừ và cộng tiền
        lockedSender.setBalance(lockedSender.getBalance().subtract(request.getAmount()));
        lockedReceiver.setBalance(lockedReceiver.getBalance().add(request.getAmount()));

        accountRepository.save(lockedSender);
        accountRepository.save(lockedReceiver);

        // 6. Lưu lịch sử giao dịch
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
    public Page<StatementResponse> getStatement(Pageable pageable) {
        Account myAccount = getMyAccount();
        Page<StatementResponse> statements = transactionRepository.findAllByAccountId(myAccount.getId(), pageable);
        
        // Gắn nhãn động CREDIT/DEBIT
        statements.forEach(statement -> {
            if (statement.getFromAccountId().equals(myAccount.getId())) {
                statement.setType(TransactionType.DEBIT); // Trừ tiền
            } else {
                statement.setType(TransactionType.CREDIT); // Cộng tiền
            }
        });
        
        return statements;
    }

    private Account getMyAccount() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new AppException(ResponseCode.BANK_ACCOUNT_NOT_FOUND));
    }
}
