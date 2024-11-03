package kz.romanb.onelabproject.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.romanb.onelabproject.OnelabProjectApplication;
import kz.romanb.onelabproject.models.dto.RegistrationRequest;
import kz.romanb.onelabproject.models.dto.UserDto;
import kz.romanb.onelabproject.models.entities.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OnelabProjectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testRegistration() throws Exception {
        RegistrationRequest request = new RegistrationRequest("user@mail.ru", "123", "username");

        mockMvc.perform(
                        post("/api/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.email").value("user@mail.ru"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void testRegistrationWithBadCredentials() throws Exception {
        RegistrationRequest request = new RegistrationRequest("user@mail.ru", "123", null);

        mockMvc.perform(
                        post("/api/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void testRegistrationWhenUserAlreadyExists() throws Exception {
        RegistrationRequest request = new RegistrationRequest("roman.bash14@mail.ru", "123", "ramioris");

        mockMvc.perform(
                        post("/api/registration")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER", "ADMIN"})
    void getAllUsers() throws Exception {
        mockMvc.perform(get("/api/users/getAllUsers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("ramioris"))
                .andExpect(jsonPath("$[0].email").value("roman.bash14@mail.ru"))
                .andExpect(jsonPath("$[0].roles").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void getAllUsersUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/getAllUsers"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER", "ADMIN"})
    void getUserById() throws Exception {
        var mvcResult = mockMvc.perform(get("/api/users/{userId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ramioris"))
                .andExpect(jsonPath("$.email").value("roman.bash14@mail.ru"))
                .andReturn();
        UserDto resultUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDto.class);
        assertTrue(resultUser.getRoles().contains(Role.USER));
        assertTrue(resultUser.getRoles().contains(Role.ADMIN));
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void getUserByIdUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", 1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER", "ADMIN"})
    void getUserByIdWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER", "ADMIN"})
    void deleteUserById() throws Exception {
        var mvcResult = mockMvc.perform(delete("/api/users/{userId}", 1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertEquals("Пользователь удален", mvcResult);
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER", "ADMIN"})
    void deleteUserByIdWhenUserDoesNotExists() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", Short.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    @WithMockUser(username = "user@mail.ru", password = "123", authorities = {"USER"})
    void deleteUserByIdUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", Short.MAX_VALUE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.description").exists());
    }
}
