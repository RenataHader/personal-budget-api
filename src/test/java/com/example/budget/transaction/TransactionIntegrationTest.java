package com.example.budget.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long accountId;

    @BeforeEach
    void setUp() throws Exception {
        accountId = createAccount("Test Account");
    }

    @Test
    void shouldIncreaseBalanceOnIncome() throws Exception {
        createTransaction(500, "INCOME", "Wynagrodzenie", "2024-03-01", accountId);

        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.0));
    }

    @Test
    void shouldDecreaseBalanceOnExpense() throws Exception {
        createTransaction(500, "INCOME", "Wynagrodzenie", "2024-03-01", accountId);
        createTransaction(200, "EXPENSE", "Jedzenie", "2024-03-02", accountId);

        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(300.0));
    }

    @Test
    void shouldRevertBalanceOnIncomeTransactionDelete() throws Exception {
        Long transactionId = createTransaction(300, "INCOME", "Wynagrodzenie", "2024-03-01", accountId);

        mockMvc.perform(delete("/transactions/" + transactionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void shouldRevertBalanceOnExpenseTransactionDelete() throws Exception {
        createTransaction(500, "INCOME", "Wynagrodzenie", "2024-03-01", accountId);
        Long expenseId = createTransaction(200, "EXPENSE", "Jedzenie", "2024-03-02", accountId);

        mockMvc.perform(delete("/transactions/" + expenseId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.0));
    }

    @Test
    void shouldReturn400WhenAmountIsNegative() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": -50,
                                  "type": "INCOME",
                                  "category": "Test",
                                  "transactionDate": "2024-03-01",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn400WhenTypeIsInvalid() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100,
                                  "type": "INVALID",
                                  "category": "Test",
                                  "transactionDate": "2024-03-01",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100,
                                  "type": "INCOME",
                                  "category": "Test",
                                  "transactionDate": "2024-03-01",
                                  "accountId": 9999
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found"));
    }

    @Test
    void shouldReturn404WhenTransactionNotFoundOnDelete() throws Exception {
        mockMvc.perform(delete("/transactions/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found"));
    }

    @Test
    void shouldFilterTransactionsByCategory() throws Exception {
        createTransaction(100, "EXPENSE", "Jedzenie", "2024-03-01", accountId);
        createTransaction(50, "EXPENSE", "Transport", "2024-03-02", accountId);

        mockMvc.perform(get("/transactions?category=Jedzenie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("Jedzenie"));
    }

    @Test
    void shouldFilterTransactionsByDateRange() throws Exception {
        createTransaction(100, "EXPENSE", "Jedzenie", "2024-03-01", accountId);
        createTransaction(50, "EXPENSE", "Transport", "2024-04-01", accountId);

        mockMvc.perform(get("/transactions?from=2024-04-01&to=2024-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("Transport"));
    }

    private Long createAccount(String name) throws Exception {
        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s"
                                }
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createTransaction(
            int amount,
            String type,
            String category,
            String transactionDate,
            Long accountId
    ) throws Exception {
        String response = mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": %d,
                                  "type": "%s",
                                  "category": "%s",
                                  "transactionDate": "%s",
                                  "accountId": %d
                                }
                                """.formatted(amount, type, category, transactionDate, accountId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}