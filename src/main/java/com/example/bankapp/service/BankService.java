
package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.exception.InsufficientBalanceException;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;

import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    // Find customer by account number
    public Customer getCustomer(String accountNumber) {
        return customerRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(accountNumber));
    }

    // Create new customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {
            throw new BankException(
                    "Account number already exists");
        }

        if (customerRepository.countEmails(
                customer.getEmail()) > 0) {
            throw new BankException(
                    "Email already exists");
        }

        if (customer.getBalance() == null ||
                customer.getBalance()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new BankException(
                    "Initial balance cannot be negative");
        }

        return customerRepository.save(customer);
    }

    // Transfer money with rollback support
    @Transactional(rollbackFor = Exception.class)
    public void transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        validateAmount(amount);

        if (fromAccount == null || toAccount == null ||
                fromAccount.equals(toAccount)) {
            throw new BankException(
                    "Sender and receiver must be different");
        }

        // Check that both accounts exist
        getCustomer(fromAccount);
        getCustomer(toAccount);

        // Atomic debit
        int debitResult = customerRepository.debit(
                fromAccount, amount);

        if (debitResult == 0) {
            throw new InsufficientBalanceException(
                    fromAccount);
        }

        // Simulate failure AFTER debit
        if (simulateFailure) {
            throw new BankException(
                    "Simulated failure after debit");
        }

        // Atomic credit
        int creditResult = customerRepository.credit(
                toAccount, amount);

        if (creditResult == 0) {
            throw new AccountNotFoundException(toAccount);
        }

        // Save successful transaction
        saveTransaction(
                fromAccount,
                toAccount,
                amount,
                "TRANSFER",
                TransactionStatus.SUCCESS,
                "Money transferred successfully");
    }

    // Compatibility with current controller
    @Transactional(rollbackFor = Exception.class)
    public void transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount) {

        transfer(fromAccount, toAccount, amount, false);
    }

    // Deposit money
    @Transactional
    public void deposit(
            String accountNumber,
            BigDecimal amount) {

        validateAmount(amount);
        getCustomer(accountNumber);

        customerRepository.credit(accountNumber, amount);

        saveTransaction(
                null,
                accountNumber,
                amount,
                "DEPOSIT",
                TransactionStatus.SUCCESS,
                "Money deposited");
    }

    // Withdraw money
    @Transactional
    public void withdraw(
            String accountNumber,
            BigDecimal amount) {

        validateAmount(amount);
        getCustomer(accountNumber);

        int result = customerRepository.debit(
                accountNumber, amount);

        if (result == 0) {
            throw new InsufficientBalanceException(
                    accountNumber);
        }

        saveTransaction(
                accountNumber,
                null,
                amount,
                "WITHDRAWAL",
                TransactionStatus.SUCCESS,
                "Money withdrawn");
    }

    // Transaction history
    public List<BankTransaction> getTransactionHistory(
            String accountNumber) {

        return transactionRepository
                .findTransactionHistory(accountNumber);
    }

    // Latest transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository
                .findLatestTransactions();
    }

    // Total balance of all customers
    public BigDecimal getTotalBalance() {
        BigDecimal total =
                customerRepository.getTotalBalance();

        return total != null ? total : BigDecimal.ZERO;
    }

    // Count transactions by status
    public long countTransactions(
            TransactionStatus status) {

        return transactionRepository.countByStatus(status);
    }

    // Search customers by name
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    // Find large transfers
    public List<BankTransaction> getLargeTransfers(
            BigDecimal amount) {

        return transactionRepository
                .findLargeTransfersByStatus(
                        amount, TransactionStatus.SUCCESS);
    }

    // Validate transaction amount
    private void validateAmount(BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BankException(
                    "Amount must be greater than zero");
        }
    }

    // Save transaction record
    private void saveTransaction(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            String type,
            TransactionStatus status,
            String description) {

        BankTransaction transaction =
                new BankTransaction();

        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setDescription(description);

        transactionRepository.save(transaction);
    }
}
