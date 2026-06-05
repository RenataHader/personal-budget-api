package com.example.budget.transaction;

import com.example.budget.account.Account;
import com.example.budget.account.AccountRepository;
import com.example.budget.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(LocalDate from, LocalDate to, String category) {
        return transactionRepository.findAllWithFilters(from, to, category)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Transaction transaction = new Transaction(
                request.amount(),
                request.type(),
                request.category(),
                request.description(),
                request.transactionDate(),
                account
        );

        applyTransactionToAccountBalance(account, transaction);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionResponse.from(savedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        revertTransactionFromAccountBalance(transaction);

        transactionRepository.delete(transaction);
    }

    private void applyTransactionToAccountBalance(Account account, Transaction transaction) {
        if (transaction.getType() == TransactionType.INCOME) {
            account.increaseBalance(transaction.getAmount());
        } else {
            account.decreaseBalance(transaction.getAmount());
        }
    }

    private void revertTransactionFromAccountBalance(Transaction transaction) {
        Account account = transaction.getAccount();

        if (transaction.getType() == TransactionType.INCOME) {
            account.decreaseBalance(transaction.getAmount());
        } else {
            account.increaseBalance(transaction.getAmount());
        }
    }
}
