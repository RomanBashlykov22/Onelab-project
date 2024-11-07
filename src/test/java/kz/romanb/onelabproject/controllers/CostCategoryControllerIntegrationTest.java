package kz.romanb.onelabproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.romanb.onelabproject.OnelabProjectApplication;
import kz.romanb.onelabproject.models.dto.CostCategoryDto;
import kz.romanb.onelabproject.models.entities.CostCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OnelabProjectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CostCategoryControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllUsersCostCategories() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/cost-categories", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sport"))
                .andExpect(jsonPath("$[0].categoryType").value(CostCategory.CostCategoryType.EXPENSE.name()))
                .andExpect(jsonPath("$[0].user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetAllUsersCostCategoriesWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/cost-categories", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetCostCategoryById() throws Exception {
        mockMvc.perform(get("/api/cost-categories/{costCategoryId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sport"))
                .andExpect(jsonPath("$.categoryType").value(CostCategory.CostCategoryType.EXPENSE.name()))
                .andExpect(jsonPath("$.user").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testGetCostCategoryByIdWhenCostCategoryDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/cost-categories/{costCategoryId}", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewCostCategory() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/cost-categories", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                CostCategoryDto.builder()
                                                        .name("New Category")
                                                        .categoryType(CostCategory.CostCategoryType.EXPENSE)
                                                        .build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.categoryType").value(CostCategory.CostCategoryType.EXPENSE.name()))
                .andExpect(jsonPath("$.user.id").value(1));
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewCostCategoryWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/cost-categories", Short.MAX_VALUE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                CostCategoryDto.builder()
                                                        .name("New Category")
                                                        .categoryType(CostCategory.CostCategoryType.EXPENSE)
                                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewCostCategoryWhenUserAlreadyHaveCostCategoryWithName() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/cost-categories", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                CostCategoryDto.builder()
                                                        .name("Sport")
                                                        .categoryType(CostCategory.CostCategoryType.EXPENSE)
                                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void testAddNewCostCategoryWhenCostCategoryTypeIsNull() throws Exception {
        mockMvc.perform(
                        post("/api/users/{userId}/cost-categories", 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                CostCategoryDto.builder()
                                                        .name("New Category")
                                                        .build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }
}
