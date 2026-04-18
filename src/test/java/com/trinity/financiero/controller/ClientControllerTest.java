package com.trinity.financiero.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trinity.financiero.dto.ClientDTO;
import com.trinity.financiero.dto.ClientResponseDTO;
import com.trinity.financiero.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    private ObjectMapper objectMapper;
    private ClientDTO validDTO;
    private ClientResponseDTO clientResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validDTO = ClientDTO.builder()
                .identificationType("CC").identificationNumber("1234567890")
                .firstName("Juan").lastName("Pérez")
                .email("juan.perez@email.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        clientResponse = ClientResponseDTO.builder()
                .id(1L).identificationType("CC").identificationNumber("1234567890")
                .firstName("Juan").lastName("Pérez")
                .email("juan.perez@email.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createClient_ValidData_Returns201() throws Exception {
        when(clientService.createClient(any())).thenReturn(clientResponse);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Juan"));
    }

    @Test
    void createClient_InvalidEmail_Returns400() throws Exception {
        validDTO.setEmail("no-es-un-email");

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createClient_Underage_Returns400() throws Exception {
        when(clientService.createClient(any()))
                .thenThrow(new IllegalArgumentException("El cliente debe ser mayor de edad"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El cliente debe ser mayor de edad"));
    }

    @Test
    void getClient_NonExistent_Returns404() throws Exception {
        when(clientService.getClientById(99L))
                .thenThrow(new EntityNotFoundException("Cliente no encontrado"));

        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteClient_WithLinkedProducts_Returns409() throws Exception {
        doThrow(new IllegalStateException("No se puede eliminar"))
                .when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void updateClient_ValidData_Returns200() throws Exception {
        when(clientService.updateClient(eq(1L), any())).thenReturn(clientResponse);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isOk());
    }
}
