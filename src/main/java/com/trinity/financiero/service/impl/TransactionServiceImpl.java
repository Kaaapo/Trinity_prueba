package com.trinity.financiero.service.impl;

import com.trinity.financiero.dto.TransactionDTO;
import com.trinity.financiero.dto.TransactionResponseDTO;
import com.trinity.financiero.entity.Account;
import com.trinity.financiero.entity.Transaction;
import com.trinity.financiero.repository.AccountRepository;
import com.trinity.financiero.repository.TransactionRepository;
import com.trinity.financiero.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private static final String TYPE_CONSIGNACION = "CONSIGNACION";
    private static final String TYPE_RETIRO = "RETIRO";
    private static final String TYPE_TRANSFERENCIA = "TRANSFERENCIA";
    private static final String ACCOUNT_TYPE_AHORROS = "AHORROS";
    private static final String STATUS_ACTIVA = "ACTIVA";

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(TransactionDTO dto) {
        String transactionType = dto.getTransactionType().toUpperCase();
        validateTransactionType(transactionType);

        return switch (transactionType) {
            case TYPE_CONSIGNACION -> processDeposit(dto);
            case TYPE_RETIRO -> processWithdrawal(dto);
            case TYPE_TRANSFERENCIA -> processTransfer(dto);
            default -> throw new IllegalArgumentException("Tipo de transacción no válido");
        };
    }

    @Override
    public List<TransactionResponseDTO> getTransactionsByAccountId(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new EntityNotFoundException("Cuenta con ID " + accountId + " no encontrada");
        }
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    private TransactionResponseDTO processDeposit(TransactionDTO dto) {
        Account account = findAccountOrThrow(dto.getAccountId());
        validateAccountActive(account);

        account.setBalance(account.getBalance().add(dto.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(TYPE_CONSIGNACION)
                .amount(dto.getAmount())
                .description(dto.getDescription() != null ? dto.getDescription() : "Consignación")
                .account(account)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponseDTO(saved);
    }

    private TransactionResponseDTO processWithdrawal(TransactionDTO dto) {
        Account account = findAccountOrThrow(dto.getAccountId());
        validateAccountActive(account);

        BigDecimal newBalance = account.getBalance().subtract(dto.getAmount());

        if (ACCOUNT_TYPE_AHORROS.equals(account.getAccountType()) && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "La cuenta de ahorros no puede tener un saldo menor a $0. Saldo actual: $" + account.getBalance());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(TYPE_RETIRO)
                .amount(dto.getAmount())
                .description(dto.getDescription() != null ? dto.getDescription() : "Retiro")
                .account(account)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponseDTO(saved);
    }

    private TransactionResponseDTO processTransfer(TransactionDTO dto) {
        if (dto.getDestinationAccountId() == null) {
            throw new IllegalArgumentException(
                    "El ID de la cuenta destino es obligatorio para transferencias");
        }

        if (dto.getAccountId().equals(dto.getDestinationAccountId())) {
            throw new IllegalArgumentException(
                    "La cuenta origen y la cuenta destino no pueden ser la misma");
        }

        Account sourceAccount = findAccountOrThrow(dto.getAccountId());
        Account destinationAccount = findAccountOrThrow(dto.getDestinationAccountId());

        validateAccountActive(sourceAccount);
        validateAccountActive(destinationAccount);

        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(dto.getAmount());

        if (ACCOUNT_TYPE_AHORROS.equals(sourceAccount.getAccountType()) && newSourceBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                    "La cuenta de ahorros origen no puede quedar con saldo menor a $0. Saldo actual: $" + sourceAccount.getBalance());
        }

        sourceAccount.setBalance(newSourceBalance);
        accountRepository.save(sourceAccount);

        destinationAccount.setBalance(destinationAccount.getBalance().add(dto.getAmount()));
        accountRepository.save(destinationAccount);

        Transaction debitTransaction = Transaction.builder()
                .transactionType(TYPE_TRANSFERENCIA)
                .amount(dto.getAmount())
                .description(dto.getDescription() != null ? dto.getDescription()
                        : "Transferencia enviada a cuenta " + destinationAccount.getAccountNumber())
                .account(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        Transaction savedDebit = transactionRepository.save(debitTransaction);

        Transaction creditTransaction = Transaction.builder()
                .transactionType(TYPE_CONSIGNACION)
                .amount(dto.getAmount())
                .description("Transferencia recibida de cuenta " + sourceAccount.getAccountNumber())
                .account(destinationAccount)
                .destinationAccount(sourceAccount)
                .build();

        transactionRepository.save(creditTransaction);

        return mapToResponseDTO(savedDebit);
    }

    private Account findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cuenta con ID " + id + " no encontrada"));
    }

    private void validateAccountActive(Account account) {
        if (!STATUS_ACTIVA.equals(account.getStatus())) {
            throw new IllegalStateException(
                    "La cuenta " + account.getAccountNumber() + " no está activa. Estado actual: " + account.getStatus());
        }
    }

    private void validateTransactionType(String type) {
        if (!TYPE_CONSIGNACION.equals(type) && !TYPE_RETIRO.equals(type) && !TYPE_TRANSFERENCIA.equals(type)) {
            throw new IllegalArgumentException(
                    "El tipo de transacción debe ser CONSIGNACION, RETIRO o TRANSFERENCIA");
        }
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        TransactionResponseDTO.TransactionResponseDTOBuilder builder = TransactionResponseDTO.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .accountId(transaction.getAccount().getId())
                .accountNumber(transaction.getAccount().getAccountNumber());

        if (transaction.getDestinationAccount() != null) {
            builder.destinationAccountId(transaction.getDestinationAccount().getId())
                    .destinationAccountNumber(transaction.getDestinationAccount().getAccountNumber());
        }

        return builder.build();
    }
}
