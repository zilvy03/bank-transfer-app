
package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Dashboard
    @GetMapping("/")
    public String dashboard(
            @RequestParam(required = false) String search,
            Model model) {

        if (search != null && !search.isBlank()) {
            model.addAttribute(
                    "customers",
                    bankService.searchCustomers(search));
        } else {
            model.addAttribute(
                    "customers",
                    bankService.getAllCustomers());
        }

        model.addAttribute("search", search);
        model.addAttribute("totalBalance",
                bankService.getTotalBalance());
        model.addAttribute("successCount",
                bankService.countTransactions(
                        TransactionStatus.SUCCESS));
        model.addAttribute("failedCount",
                bankService.countTransactions(
                        TransactionStatus.FAILED));
        model.addAttribute("transactions",
                bankService.getLatestTransactions());
        model.addAttribute("transferRequest",
                new TransferRequest());
        model.addAttribute("customer", new Customer());

        return "bank";
    }

    // Add a new customer
    @PostMapping("/customers")
    public String createCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please check customer details");
            return "redirect:/";
        }

        try {
            bankService.createCustomer(customer);
            redirectAttributes.addFlashAttribute(
                    "success", "Customer created successfully");
        } catch (BankException ex) {
            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());
        }

        return "redirect:/";
    }

    // Transfer money
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest")
                    TransferRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "Please check transfer details");
            return "redirect:/";
        }

        try {
            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure());

            redirectAttributes.addFlashAttribute(
                    "success", "Transfer successful");

        } catch (BankException ex) {

            auditService.recordFailedTransfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    ex.getMessage());

            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage());
        }

        return "redirect:/";
    }

    // Account transaction history
    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(
            @PathVariable String accountNumber,
            Model model) {

        model.addAttribute(
                "customer",
                bankService.getCustomer(accountNumber));

        model.addAttribute(
                "transactions",
                bankService.getTransactionHistory(accountNumber));

        return "history";
    }
}
