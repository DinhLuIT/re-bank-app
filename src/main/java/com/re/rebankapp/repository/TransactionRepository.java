package com.re.rebankapp.repository;

import com.re.rebankapp.dto.response.StatementResponse;
import com.re.rebankapp.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT new com.re.rebankapp.dto.response.StatementResponse(" +
            "t.transactionCode, t.amount, t.description, t.createdAt, t.fromAccount.id, t.toAccount.id) " +
            "FROM Transaction t " +
            "WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId " +
            "ORDER BY t.createdAt DESC")
    Page<StatementResponse> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.fromAccount.id = :accountId " +
            "AND FUNCTION('DATE', t.createdAt) = CURRENT_DATE")
    BigDecimal sumDailyOutflow(@Param("accountId") Long accountId);
}
