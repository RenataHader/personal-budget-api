package com.example.budget.account;

import java.math.BigDecimal;

public record AccountResponse(Long id, String name, BigDecimal balance) {

    public static AccountResponse from(Account account){
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }
}
