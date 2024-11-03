package kz.romanb.onelabproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.romanb.onelabproject.OnelabProjectApplication;
import kz.romanb.onelabproject.models.dto.BankAccountDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OnelabProjectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BankAccountControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllUsersBankAccounts() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/bank-accounts", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Kaspi"))
                .andExpect(jsonPath("$[0].balance").isNotEmpty())
                .andExpect(jsonPath("$[0].user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllUsersBankAccountsWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/bank-accounts", Short.MAX_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetBankAccountById() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/{bankAccountId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Kaspi"))
                .andExpect(jsonPath("$.balance").isNotEmpty())
                .andExpect(jsonPath("$.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetBankAccountByIdWhenBankAccountDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/bank-accounts/{bankAccountId}", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewBankAccount() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/bank-accounts", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                BankAccountDto.builder()
                                                        .name("Halyk")
                                                        .balance(new BigDecimal(300))
                                                        .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Halyk"))
                .andExpect(jsonPath("$.balance").value(300))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewBankAccountWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/bank-accounts", Short.MAX_VALUE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                BankAccountDto.builder()
                                                        .name("Halyk")
                                                        .balance(new BigDecimal(300))
                                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewBankAccountWhenUserAlreadyHaveBankAccountWithName() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/bank-accounts", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                BankAccountDto.builder()
                                                        .name("Kaspi")
                                                        .balance(new BigDecimal(300))
                                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewBankAccountWhenBalanceIsNull() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/bank-accounts", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                BankAccountDto.builder()
                                                        .name("Halyk")
                                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testChangeBalance() throws Exception {
        var mvcResult = mockMvc.perform(
                        patch("/api/bank-accounts/{bankAccountId}", 1)
                                .param("amount", "666.8"))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals("Баланс изменен", mvcResult.getResponse().getContentAsString());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testChangeBalanceWhenBankAccountDoesNotExists() throws Exception {
        mockMvc.perform(
                        patch("/api/bank-accounts/{bankAccountId}", Short.MAX_VALUE)
                                .param("amount", "666.8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testChangeBalanceWhenBalanceLessThanZero() throws Exception {
        mockMvc.perform(
                        patch("/api/bank-accounts/{bankAccountId}", 1)
                                .param("amount", "-666.8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }
}
