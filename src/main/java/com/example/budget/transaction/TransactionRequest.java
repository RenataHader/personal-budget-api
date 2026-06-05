package com.example.budget.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Transaction type must not be null")
        TransactionType type,

        @NotBlank(message = "Category must not be blank")
        @Size(max = 255, message = "Category must not be longer than 255 characters")
        String category,

        String description,

        @NotNull(message = "Transaction date must not be null")
        LocalDate transactionDate,

        @NotNull(message = "Account id must not be null")
        Long accountId

) {
}
