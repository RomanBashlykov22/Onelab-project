package kz.romanb.onelabproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.romanb.onelabproject.OnelabProjectApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OnelabProjectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OperationControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ModelMapper modelMapper;

    Integer operationId = 2;

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testCreateOperation() throws Exception {
        mockMvc.perform(
                        post("/api/operations/create")
                                .param("bankAccountId", "1")
                                .param("costCategoryId", "1")
                                .param("amount", "100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(++operationId))
                .andExpect(jsonPath("$.amount").value(100))
                .andExpect(jsonPath("$.date").value(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .andExpect(jsonPath("$.bankAccount.id").value(1))
                .andExpect(jsonPath("$.bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$.costCategory.id").value(1))
                .andExpect(jsonPath("$.costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testCreateOperationWhenBankAccountDoesNotExists() throws Exception {
        mockMvc.perform(
                        post("/api/operations/create")
                                .param("bankAccountId", Short.MAX_VALUE + "")
                                .param("costCategoryId", "1")
                                .param("amount", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testCreateOperationWhenCostCategoryDoesNotExists() throws Exception {
        mockMvc.perform(
                        post("/api/operations/create")
                                .param("bankAccountId", "1")
                                .param("costCategoryId", Short.MAX_VALUE + "")
                                .param("amount", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testCreateOperationWhenNotEnoughMoney() throws Exception {
        mockMvc.perform(
                        post("/api/operations/create")
                                .param("bankAccountId", "1")
                                .param("costCategoryId", "1")
                                .param("amount", "1000000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetOperationById() throws Exception {
        mockMvc.perform(get("/api/operations/{operationId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").isNotEmpty())
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.bankAccount.id").value(1))
                .andExpect(jsonPath("$.bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$.costCategory.id").value(1))
                .andExpect(jsonPath("$.costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetOperationByIdWhenOperationDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/operations/{operationId}", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetOperations() throws Exception {
        mockMvc.perform(get("/api/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(operationId))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[2].id").value(2))
                .andExpect(jsonPath("$[0].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[0].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[1].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[1].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[2].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[2].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory.user").isNotEmpty())
                .andExpect(jsonPath("$[1].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[1].costCategory.user").isNotEmpty())
                .andExpect(jsonPath("$[2].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[2].costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetOperationsForDate() throws Exception {
        mockMvc.perform(
                        get("/api/operations")
                                .param("fromDate", "01.10.2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].date").value("01.10.2024"))
                .andExpect(jsonPath("$[0].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[0].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetOperationsBetweenDates() throws Exception {
        mockMvc.perform(
                        get("/api/operations")
                                .param("fromDate", "01.10.2024")
                                .param("toDate", "13.10.2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].date").value("13.10.2024"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].date").value("01.10.2024"))
                .andExpect(jsonPath("$[0].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[0].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[1].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[1].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory.user").isNotEmpty())
                .andExpect(jsonPath("$[1].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[1].costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllOperationsByUser() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/operations", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(operationId))
                .andExpect(jsonPath("$[0].date").value(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].date").value("13.10.2024"))
                .andExpect(jsonPath("$[2].id").value(2))
                .andExpect(jsonPath("$[2].date").value("01.10.2024"))
                .andExpect(jsonPath("$[0].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[0].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[1].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[1].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[2].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[2].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory.user").isNotEmpty())
                .andExpect(jsonPath("$[1].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[1].costCategory.user").isNotEmpty())
                .andExpect(jsonPath("$[2].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[2].costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllOperationsByUserWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/operations", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllOperationsByCostCategory() throws Exception {
        mockMvc.perform(get("/api/cost-categories/{costCategoryId}/operations", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].date").value("13.10.2024"))
                .andExpect(jsonPath("$[0].bankAccount").isNotEmpty())
                .andExpect(jsonPath("$[0].bankAccount.user").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory").isNotEmpty())
                .andExpect(jsonPath("$[0].costCategory.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllOperationsByCostCategoryWhenCostCategoryDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/cost-categories/{costCategoryId}/operations", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetSum() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/operations"))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(
                        get("/api/sum")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mvcResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountOfOperations").value(operationId))
                .andExpect(jsonPath("$.expense").value("6100.0"))
                .andExpect(jsonPath("$.income").value("9831.07"));
    }
}
