
package com.example.bankapp.repository;

import com.example.bankapp.model.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    // 1. Find customer by account number
    @Query("SELECT c FROM Customer c WHERE c.accountNumber = :accountNumber")
    Optional<Customer> getByAccountNumber(
            @Param("accountNumber") String accountNumber);

    // Compatibility with existing BankService
    Optional<Customer> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    // 2. Count duplicate accounts
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.accountNumber = :accountNumber")
    long countAccounts(
            @Param("accountNumber") String accountNumber);

    // Count duplicate emails
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email = :email")
    long countEmails(@Param("email") String email);

    // 3. Sort customers by name
    @Query("SELECT c FROM Customer c ORDER BY c.name ASC")
    List<Customer> findAllOrdered();

    // 4. Case-insensitive name search
    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByName(
            @Param("keyword") String keyword);

    // 5. Total balance
    @Query("SELECT SUM(c.balance) FROM Customer c")
    BigDecimal getTotalBalance();

    // 6. Native SQL customer count
    @Query(value = "SELECT COUNT(*) FROM customers", nativeQuery = true)
    long countCustomersNative();

    // 7. Atomic debit
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Customer c SET c.balance = c.balance - :amount " +
           "WHERE c.accountNumber = :accountNumber " +
           "AND c.balance >= :amount")
    int debit(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount);

    // 8. Atomic credit
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Customer c SET c.balance = c.balance + :amount " +
           "WHERE c.accountNumber = :accountNumber")
    int credit(
            @Param("accountNumber") String accountNumber,
            @Param("amount") BigDecimal amount);
}

