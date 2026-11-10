
package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {

        // Add sample customers only if database is empty
        if (customerRepository.count() > 0) {
            return;
        }

        createCustomer(
                "Kamal",
                "kamal@example.com",
                "100001",
                "50000.00"
        );

        createCustomer(
                "Nimali",
                "nimali@example.com",
                "100002",
                "25000.00"
        );

        createCustomer(
                "Saman",
                "saman@example.com",
                "100003",
                "10000.00"
        );
    }

    private void createCustomer(
            String name,
            String email,
            String accountNumber,
            String balance) {

        Customer customer = new Customer();

        customer.setName(name);
        customer.setEmail(email);
        customer.setAccountNumber(accountNumber);
        customer.setBalance(new BigDecimal(balance));

        customerRepository.save(customer);
    }
}
