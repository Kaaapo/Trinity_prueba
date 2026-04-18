package com.trinity.financiero.service;

import com.trinity.financiero.dto.TransactionDTO;
import com.trinity.financiero.dto.TransactionResponseDTO;
import com.trinity.financiero.entity.Account;
import com.trinity.financiero.entity.Client;
import com.trinity.financiero.entity.Transaction;
import com.trinity.financiero.repository.AccountRepository;
import com.trinity.financiero.repository.TransactionRepository;
import com.trinity.financiero.service.impl.TransactionServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account savingsAccount;
    private Account checkingAccount;

    @BeforeEach
    void setUp() {
        Client client = Client.builder()
                .id(1L).firstName("Juan").lastName("Pérez")
                .identificationType("CC").identificationNumber("123")
                .email("juan@email.com").birthDate(LocalDate.of(1990, 1, 1))
                .build();

        savingsAccount = Account.builder()
                .id(1L).accountType("AHORROS").accountNumber("5312345678")
                .status("ACTIVA").balance(BigDecimal.valueOf(500000)).client(client)
                .build();

        checkingAccount = Account.builder()
                .id(2L).accountType("CORRIENTE").accountNumber("3312345678")
                .status("ACTIVA").balance(BigDecimal.valueOf(1000000)).client(client)
                .build();
    }

    private Transaction buildTransaction(String type, Account account) {
        return Transaction.builder()
                .id(1L).transactionType(type)
                .amount(BigDecimal.valueOf(100000))
                .account(account).createdAt(LocalDateTime.now())
                .build();
    }



    @Test
    void deposit_Success_BalanceIncreases() {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("CONSIGNACION").accountId(1L)
                .amount(BigDecimal.valueOf(100000)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        when(transactionRepository.save(any())).thenReturn(buildTransaction("CONSIGNACION", savingsAccount));

        transactionService.createTransaction(dto);

        assertEquals(BigDecimal.valueOf(600000), savingsAccount.getBalance());
    }



    @Test
    void withdrawal_Success_BalanceDecreases() {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("RETIRO").accountId(1L)
                .amount(BigDecimal.valueOf(200000)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        when(transactionRepository.save(any())).thenReturn(buildTransaction("RETIRO", savingsAccount));

        transactionService.createTransaction(dto);

        assertEquals(BigDecimal.valueOf(300000), savingsAccount.getBalance());
    }

    @Test
    void withdrawal_SavingsGoNegative_ThrowsIllegalState() {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("RETIRO").accountId(1L)
                .amount(BigDecimal.valueOf(600000)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> transactionService.createTransaction(dto));

        assertTrue(ex.getMessage().contains("saldo menor a $0"));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdrawal_CheckingGoNegative_Allowed() {
        checkingAccount.setBalance(BigDecimal.valueOf(100000));

        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("RETIRO").accountId(2L)
                .amount(BigDecimal.valueOf(200000)).build();

        when(accountRepository.findById(2L)).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.save(any())).thenReturn(checkingAccount);
        when(transactionRepository.save(any())).thenReturn(buildTransaction("RETIRO", checkingAccount));

        assertDoesNotThrow(() -> transactionService.createTransaction(dto));
        assertEquals(BigDecimal.valueOf(-100000), checkingAccount.getBalance());
    }



    @Test
    void transfer_Success_UpdatesBothBalancesAndSavesTwice() {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("TRANSFERENCIA").accountId(1L)
                .destinationAccountId(2L).amount(BigDecimal.valueOf(100000)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any())).thenReturn(buildTransaction("TRANSFERENCIA", savingsAccount));

        transactionService.createTransaction(dto);

        assertEquals(BigDecimal.valueOf(400000), savingsAccount.getBalance());
        assertEquals(BigDecimal.valueOf(1100000), checkingAccount.getBalance());
        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void transfer_DestinationNotFound_ThrowsEntityNotFound() {
        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("TRANSFERENCIA").accountId(1L)
                .destinationAccountId(99L).amount(BigDecimal.valueOf(100000)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> transactionService.createTransaction(dto));
    }

    @Test
    void transfer_SavingsInsufficientFunds_ThrowsIllegalState() {
        savingsAccount.setBalance(BigDecimal.valueOf(50000));

        TransactionDTO dto = TransactionDTO.builder()
                .transactionType("TRANSFERENCIA").accountId(1L)
                .destinationAccountId(2L).amount(BigDecimal.valueOf(100000)).build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(checkingAccount));

        assertThrows(IllegalStateException.class, () -> transactionService.createTransaction(dto));
        verify(accountRepository, never()).save(any());
    }
}
