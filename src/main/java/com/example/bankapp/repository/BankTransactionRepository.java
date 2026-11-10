
package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BankTransactionRepository
        extends JpaRepository<BankTransaction, Long> {

    // Latest transactions first
    @Query("SELECT t FROM BankTransaction t " +
           "ORDER BY t.transactionDate DESC")
    List<BankTransaction> findLatestTransactions();

    // Transaction history for an account
    @Query("SELECT t FROM BankTransaction t " +
           "WHERE t.fromAccount = :accountNumber " +
           "OR t.toAccount = :accountNumber " +
           "ORDER BY t.transactionDate DESC")
    List<BankTransaction> findTransactionHistory(
            @Param("accountNumber") String accountNumber);

    // Filter transactions by status
    @Query("SELECT t FROM BankTransaction t " +
           "WHERE t.status = :status")
    List<BankTransaction> findByTransactionStatus(
            @Param("status") TransactionStatus status);

    // Count transactions by status
    @Query("SELECT COUNT(t) FROM BankTransaction t " +
           "WHERE t.status = :status")
    long countByStatus(
            @Param("status") TransactionStatus status);

    // Find large successful transfers
    @Query("SELECT t FROM BankTransaction t " +
           "WHERE t.type = 'TRANSFER' " +
           "AND t.status = :status " +
           "AND t.amount > :amount " +
           "ORDER BY t.amount DESC")
    List<BankTransaction> findLargeTransfersByStatus(
            @Param("amount") BigDecimal amount,
            @Param("status") TransactionStatus status);

    // Compatibility with existing BankService
    @Query("SELECT t FROM BankTransaction t " +
           "WHERE t.amount > :amount")
    List<BankTransaction> findLargeTransfers(
            @Param("amount") BigDecimal amount);
}

