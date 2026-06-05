package com.example.budget.summary;

import com.example.budget.transaction.TransactionRepository;
import com.example.budget.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SummaryService {

    private final TransactionRepository transactionRepository;

    public SummaryService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary() {
        BigDecimal totalIncome = Optional
                .ofNullable(transactionRepository.sumAmountByType(TransactionType.INCOME))
                .orElse(BigDecimal.ZERO);

        BigDecimal totalExpenses = Optional
                .ofNullable(transactionRepository.sumAmountByType(TransactionType.EXPENSE))
                .orElse(BigDecimal.ZERO);

        Map<String, BigDecimal> expensesByCategory = transactionRepository
                .sumAmountByCategoryForType(TransactionType.EXPENSE)
                .stream()
                .collect(Collectors.toMap(
                        CategoryTotal::category,
                        CategoryTotal::total,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        return new SummaryResponse(totalIncome, totalExpenses, expensesByCategory);
    }
}