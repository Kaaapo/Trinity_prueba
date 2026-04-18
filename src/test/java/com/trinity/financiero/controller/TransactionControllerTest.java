package com.trinity.financiero.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.financiero.dto.TransactionDTO;
import com.trinity.financiero.dto.TransactionResponseDTO;
import com.trinity.financiero.service.TransactionService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    private ObjectMapper objectMapper;
    private TransactionResponseDTO depositResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        depositResponse = TransactionResponseDTO.builder()
                .id(1L).transactionType("CONSIGNACION")
                .amount(BigDecimal.valueOf(100000))
                .description("Depósito").createdAt(LocalDateTime.now())
                .accountId(1L).accountNumber("5312345678")
                .build();
    }

    @Test
    void createDeposit_ValidData_Returns201() throws Exception {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("CONSIGNACION").accountId(1L)
                .amount(BigDecimal.valueOf(100000)).build();

        when(transactionService.createTransaction(any())).thenReturn(depositResponse);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("CONSIGNACION"))
                .andExpect(jsonPath("$.amount").value(100000));
    }

    @Test
    void createWithdrawal_InsufficientFunds_Returns409() throws Exception {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("RETIRO").accountId(1L)
                .amount(BigDecimal.valueOf(9999999)).build();

        when(transactionService.createTransaction(any()))
                .thenThrow(new IllegalStateException("Saldo insuficiente"));

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createTransfer_ValidData_Returns201WithBothAccounts() throws Exception {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("TRANSFERENCIA").accountId(1L)
                .destinationAccountId(2L).amount(BigDecimal.valueOf(50000)).build();

        TransactionResponseDTO transferResponse = TransactionResponseDTO.builder()
                .id(2L).transactionType("TRANSFERENCIA")
                .amount(BigDecimal.valueOf(50000))
                .accountId(1L).accountNumber("5312345678")
                .destinationAccountId(2L).destinationAccountNumber("3312345678")
                .createdAt(LocalDateTime.now()).build();

        when(transactionService.createTransaction(any())).thenReturn(transferResponse);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionType").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.destinationAccountNumber").value("3312345678"));
    }

    @Test
    void getTransactions_AccountNotFound_Returns404() throws Exception {
        when(transactionService.getTransactionsByAccountId(99L))
                .thenThrow(new EntityNotFoundException("Cuenta no encontrada"));

        mockMvc.perform(get("/api/transactions/account/99"))
                .andExpect(status().isNotFound());
    }
}
