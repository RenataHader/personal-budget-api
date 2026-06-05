package com.example.budget.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(
            value = "SELECT COUNT(*) > 0 FROM transactions WHERE account_id = :accountId",
            nativeQuery = true
    )
    boolean hasTransactions(@Param("accountId") Long accountId);
}