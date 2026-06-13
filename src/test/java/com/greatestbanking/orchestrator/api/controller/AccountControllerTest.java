package com.greatestbanking.orchestrator.api.controller;

import com.greatestbanking.orchestrator.api.auth.JwtService;
import com.greatestbanking.orchestrator.api.dto.response.AccountResponse;
import com.greatestbanking.orchestrator.api.exception.ResourceNotFoundException;
import com.greatestbanking.orchestrator.api.service.AccountService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private RateLimiter apiRateLimiter;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setupRateLimiter() {
        when(apiRateLimiter.acquirePermission()).thenReturn(true);
    }

    @Test
    void createAccount_shouldReturn201() throws Exception {
        when(accountService.createAccount(any()))
            .thenReturn(new AccountResponse(1L, "12345678900"));

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"document_number\": \"12345678900\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.account_id").value(1))
            .andExpect(jsonPath("$.document_number").value("12345678900"));
    }

    @Test
    void createAccount_shouldReturn400_whenDocumentNumberMissing() throws Exception {
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_shouldReturn200() throws Exception {
        when(accountService.getAccount(1L))
            .thenReturn(new AccountResponse(1L, "12345678900"));

        mockMvc.perform(get("/accounts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account_id").value(1))
            .andExpect(jsonPath("$.document_number").value("12345678900"));
    }

    @Test
    void getAccount_shouldReturn404_whenNotFound() throws Exception {
        when(accountService.getAccount(99L))
            .thenThrow(new ResourceNotFoundException("Account not found with id: 99"));

        mockMvc.perform(get("/accounts/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Account not found with id: 99"));
    }
}
