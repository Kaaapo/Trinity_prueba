package com.trinity.financiero.service;

import com.trinity.financiero.dto.AccountDTO;
import com.trinity.financiero.dto.AccountResponseDTO;
import com.trinity.financiero.entity.Account;
import com.trinity.financiero.entity.Client;
import com.trinity.financiero.repository.AccountRepository;
import com.trinity.financiero.repository.ClientRepository;
import com.trinity.financiero.service.impl.AccountServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Client testClient;
    private Account savingsAccount;
    private Account checkingAccount;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .firstName("Juan").lastName("Pérez")
                .identificationType("CC").identificationNumber("123")
                .email("juan@email.com").birthDate(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        savingsAccount = Account.builder()
                .id(1L).accountType("AHORROS").accountNumber("5312345678")
                .status("ACTIVA").balance(BigDecimal.valueOf(100000))
                .gmfExempt(false).client(testClient)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        checkingAccount = Account.builder()
                .id(2L).accountType("CORRIENTE").accountNumber("3312345678")
                .status("ACTIVA").balance(BigDecimal.valueOf(500000))
                .gmfExempt(false).client(testClient)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }



    @Test
    void createAccount_Savings_PrefixIs53AndStatusActiva() {
        AccountDTO dto = AccountDTO.builder()
                .accountType("AHORROS").clientId(1L).gmfExempt(false).build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savingsAccount);

        AccountResponseDTO result = accountService.createAccount(dto);

        assertEquals("AHORROS", result.getAccountType());
        assertEquals("ACTIVA", result.getStatus());
        assertTrue(result.getAccountNumber().startsWith("53"),
                "El número de cuenta de ahorros debe iniciar en 53");
    }

    @Test
    void createAccount_Checking_PrefixIs33() {
        AccountDTO dto = AccountDTO.builder()
                .accountType("CORRIENTE").clientId(1L).build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(checkingAccount);

        AccountResponseDTO result = accountService.createAccount(dto);

        assertTrue(result.getAccountNumber().startsWith("33"),
                "El número de cuenta corriente debe iniciar en 33");
    }

    @Test
    void createAccount_ClientNotFound_ThrowsEntityNotFound() {
        AccountDTO dto = AccountDTO.builder()
                .accountType("AHORROS").clientId(99L).build();

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> accountService.createAccount(dto));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_InvalidType_ThrowsIllegalArgument() {
        AccountDTO dto = AccountDTO.builder()
                .accountType("INVALIDA").clientId(1L).build();

        assertThrows(IllegalArgumentException.class, () -> accountService.createAccount(dto));
    }



    @Test
    void updateStatus_Cancel_ZeroBalance_Succeeds() {
        savingsAccount.setBalance(BigDecimal.ZERO);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);

        assertDoesNotThrow(() -> accountService.updateAccountStatus(1L, "CANCELADA"));
        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void updateStatus_Cancel_NonZeroBalance_ThrowsIllegalState() {
        savingsAccount.setBalance(BigDecimal.valueOf(50000));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> accountService.updateAccountStatus(1L, "CANCELADA"));

        assertTrue(ex.getMessage().contains("saldo igual a $0"));
        verify(accountRepository, never()).save(any());
    }
}
