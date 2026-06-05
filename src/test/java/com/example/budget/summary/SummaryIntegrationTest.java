package com.example.budget.summary;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SummaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnZerosWhenNoTransactions() throws Exception {
        mockMvc.perform(get("/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0.0))
                .andExpect(jsonPath("$.totalExpenses").value(0.0))
                .andExpect(jsonPath("$.expensesByCategory").isMap())
                .andExpect(jsonPath("$.expensesByCategory").isEmpty());
    }

    @Test
    void shouldReturnCorrectSummary() throws Exception {
        Long accountId = createAccount("Test Account");

        createTransaction(1000, "INCOME", "Wynagrodzenie", "2024-03-01", accountId);
        createTransaction(200, "EXPENSE", "Jedzenie", "2024-03-02", accountId);
        createTransaction(50, "EXPENSE", "Jedzenie", "2024-03-03", accountId);
        createTransaction(100, "EXPENSE", "Transport", "2024-03-04", accountId);

        mockMvc.perform(get("/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1000.0))
                .andExpect(jsonPath("$.totalExpenses").value(350.0))
                .andExpect(jsonPath("$.expensesByCategory.Jedzenie").value(250.0))
                .andExpect(jsonPath("$.expensesByCategory.Transport").value(100.0));
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