package com.trinity.financiero.service;

import com.trinity.financiero.dto.ClientDTO;
import com.trinity.financiero.dto.ClientResponseDTO;
import com.trinity.financiero.entity.Client;
import com.trinity.financiero.repository.AccountRepository;
import com.trinity.financiero.repository.ClientRepository;
import com.trinity.financiero.service.impl.ClientServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    private ClientDTO validDTO;
    private Client savedClient;

    @BeforeEach
    void setUp() {
        validDTO = ClientDTO.builder()
                .identificationType("CC")
                .identificationNumber("1234567890")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@email.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        savedClient = Client.builder()
                .id(1L)
                .identificationType("CC")
                .identificationNumber("1234567890")
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan.perez@email.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }



    @Test
    void createClient_ValidAdult_ReturnsCreatedClient() {
        when(clientRepository.existsByIdentificationTypeAndIdentificationNumber(anyString(), anyString()))
                .thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        ClientResponseDTO result = clientService.createClient(validDTO);

        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("juan.perez@email.com", result.getEmail());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void createClient_Underage_ThrowsIllegalArgument() {
        validDTO.setBirthDate(LocalDate.now().minusYears(17));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clientService.createClient(validDTO));

        assertTrue(ex.getMessage().contains("mayor de edad"));
        verify(clientRepository, never()).save(any());
    }



    @Test
    void updateClient_ExistingClient_SavesCalled() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        ClientResponseDTO result = clientService.updateClient(1L, validDTO);

        assertNotNull(result);
        verify(clientRepository, times(1)).save(any(Client.class));
    }



    @Test
    void deleteClient_WithoutLinkedAccounts_DeletesCalled() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(accountRepository.existsByClientId(1L)).thenReturn(false);

        assertDoesNotThrow(() -> clientService.deleteClient(1L));
        verify(clientRepository, times(1)).delete(savedClient);
    }

    @Test
    void deleteClient_WithLinkedAccounts_ThrowsIllegalState() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(savedClient));
        when(accountRepository.existsByClientId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> clientService.deleteClient(1L));

        assertTrue(ex.getMessage().contains("productos financieros vinculados"));
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void getClient_NonExistent_ThrowsEntityNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> clientService.getClientById(99L));
    }
}
