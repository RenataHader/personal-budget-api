package com.example.budget.transaction;

import com.example.budget.account.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    protected Transaction() {}

    public Transaction(
            BigDecimal amount,
            TransactionType type,
            String category,
            String description,
            LocalDate transactionDate,
            Account account
    ) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
        this.transactionDate = transactionDate;
        this.account = account;
    }
}