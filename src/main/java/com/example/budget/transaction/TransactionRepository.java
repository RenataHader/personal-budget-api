package com.example.budget.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE (:from IS NULL OR t.transactionDate >= :from)
              AND (:to IS NULL OR t.transactionDate <= :to)
              AND (:category IS NULL OR LOWER(t.category) = LOWER(:category))
            ORDER BY t.transactionDate DESC, t.id DESC
            """)
    List<Transaction> findAllWithFilters(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("category") String category
    );
}
