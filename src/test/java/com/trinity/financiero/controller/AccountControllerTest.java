package com.trinity.financiero.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.financiero.dto.AccountDTO;
import com.trinity.financiero.dto.AccountResponseDTO;
import com.trinity.financiero.dto.AccountStatusDTO;
import com.trinity.financiero.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private ObjectMapper objectMapper;
    private AccountResponseDTO accountResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        accountResponse = AccountResponseDTO.builder()
                .id(1L).accountType("AHORROS").accountNumber("5312345678")
                .status("ACTIVA").balance(BigDecimal.ZERO).gmfExempt(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .clientId(1L).clientName("Juan Pérez")
                .build();
    }

    @Test
    void createAccount_ValidData_Returns201() throws Exception {
        AccountDTO dto = AccountDTO.builder()
                .accountType("AHORROS").clientId(1L).build();

        when(accountService.createAccount(any())).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountType").value("AHORROS"))
                .andExpect(jsonPath("$.status").value("ACTIVA"))
                .andExpect(jsonPath("$.accountNumber").value("5312345678"));
    }

    @Test
    void createAccount_ClientNotFound_Returns404() throws Exception {
        AccountDTO dto = AccountDTO.builder()
                .accountType("AHORROS").clientId(99L).build();

        when(accountService.createAccount(any()))
                .thenThrow(new EntityNotFoundException("Cliente no encontrado"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_CancelWithBalance_Returns409() throws Exception {
        AccountStatusDTO statusDTO = AccountStatusDTO.builder().status("CANCELADA").build();

        when(accountService.updateAccountStatus(eq(1L), eq("CANCELADA")))
                .thenThrow(new IllegalStateException("Solo se pueden cancelar cuentas con saldo $0"));

        mockMvc.perform(patch("/api/accounts/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAccount_NonExistent_Returns404() throws Exception {
        when(accountService.getAccountById(99L))
                .thenThrow(new EntityNotFoundException("Cuenta no encontrada"));

        mockMvc.perform(get("/api/accounts/99"))
                .andExpect(status().isNotFound());
    }
}
