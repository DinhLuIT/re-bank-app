package com.re.rebankapp.service;

import com.re.rebankapp.dto.request.TransferRequest;
import com.re.rebankapp.dto.response.StatementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TransactionService {
    
    BigDecimal getBalance();
    
    void transfer(TransferRequest request);
    
    Page<StatementResponse> getStatement(Pageable pageable);
}
