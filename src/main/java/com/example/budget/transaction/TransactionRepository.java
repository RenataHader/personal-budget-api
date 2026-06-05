package com.example.budget.transaction;

import com.example.budget.summary.CategoryTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

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
    @Query("""
        SELECT SUM(t.amount)
        FROM Transaction t
        WHERE t.type = :type
        """)
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("""
        SELECT new com.example.budget.summary.CategoryTotal(t.category, SUM(t.amount))
        FROM Transaction t
        WHERE t.type = :type
        GROUP BY t.category
        ORDER BY t.category
        """)
    List<CategoryTotal> sumAmountByCategoryForType(@Param("type") TransactionType type);
}
