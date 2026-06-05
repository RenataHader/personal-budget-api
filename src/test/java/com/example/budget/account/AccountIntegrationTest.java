package com.example.budget.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnEmptyListWhenNoAccounts() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldCreateAccount() throws Exception {
        AccountRequest request = new AccountRequest("Konto główne");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Konto główne"))
                .andExpect(jsonPath("$.balance").value(0.0));
    }

    @Test
    void shouldReturn400WhenAccountNameIsBlank() throws Exception {
        AccountRequest request = new AccountRequest("");

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        mockMvc.perform(get("/accounts/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found"));
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        Long accountId = createAccount("Do usunięcia");

        mockMvc.perform(delete("/accounts/" + accountId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn409WhenDeletingAccountWithTransactions() throws Exception {
        Long accountId = createAccount("Konto z transakcją");

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 100,
                                  "type": "INCOME",
                                  "category": "Test",
                                  "transactionDate": "2024-01-01",
                                  "accountId": %d
                                }
                                """.formatted(accountId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/accounts/" + accountId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete account with existing transactions"));
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
}