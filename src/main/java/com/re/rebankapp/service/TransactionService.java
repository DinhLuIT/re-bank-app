package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.AtmTransactionRequest;
import com.re.rebankapp.dto.request.InterbankTransferRequest;
import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.dto.response.StatementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TransactionService {
    BigDecimal getBalance(Long accountId);

    void transfer(TransferRequest request);

    void atmDeposit(AtmTransactionRequest request);

    void atmWithdraw(AtmTransactionRequest request);

    Page<StatementResponse> getStatement(Long accountId, Pageable pageable);

    void interbankTransfer(InterbankTransferRequest request);
}
