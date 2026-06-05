package com.example.budget.account;

import com.example.budget.account.AccountResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.budget.error.ConflictException;
import com.example.budget.error.NotFoundException;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts(){
        return accountRepository.findAll().stream().map(AccountResponse::from).toList();
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {

        Account account = new Account(request.name());
        Account savedAccount = accountRepository.save(account);

        return AccountResponse.from(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id){

        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));

        return AccountResponse.from(account);
    }

    @Transactional
    public void deleteAccount(Long id) {

        Account account = accountRepository.findById(id).orElseThrow(() -> new NotFoundException("Account not found"));

        if (accountRepository.hasTransactions(id)) {
            throw new ConflictException("Cannot delete account with existing transactions");
        }

        accountRepository.delete(account);
    }
}
