package com.trinity.financiero.service.impl;

import com.trinity.financiero.dto.AccountDTO;
import com.trinity.financiero.dto.AccountResponseDTO;
import com.trinity.financiero.entity.Account;
import com.trinity.financiero.entity.Client;
import com.trinity.financiero.repository.AccountRepository;
import com.trinity.financiero.repository.ClientRepository;
import com.trinity.financiero.service.AccountService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    private static final String ACCOUNT_TYPE_AHORROS = "AHORROS";
    private static final String ACCOUNT_TYPE_CORRIENTE = "CORRIENTE";
    private static final String STATUS_ACTIVA = "ACTIVA";
    private static final String STATUS_INACTIVA = "INACTIVA";
    private static final String STATUS_CANCELADA = "CANCELADA";

    @Override
    @Transactional
    public AccountResponseDTO createAccount(AccountDTO dto) {
        String accountType = dto.getAccountType().toUpperCase();
        validateAccountType(accountType);

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente con ID " + dto.getClientId() + " no encontrado"));

        String accountNumber = generateUniqueAccountNumber(accountType);

        Account account = Account.builder()
                .accountType(accountType)
                .accountNumber(accountNumber)
                .status(STATUS_ACTIVA)
                .balance(BigDecimal.ZERO)
                .gmfExempt(dto.isGmfExempt())
                .client(client)
                .build();

        Account saved = accountRepository.save(account);
        return mapToResponseDTO(saved);
    }

    @Override
    public AccountResponseDTO getAccountById(Long id) {
        Account account = findAccountOrThrow(id);
        return mapToResponseDTO(account);
    }

    @Override
    public List<AccountResponseDTO> getAccountsByClientId(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new EntityNotFoundException("Cliente con ID " + clientId + " no encontrado");
        }
        return accountRepository.findByClientId(clientId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public AccountResponseDTO updateAccountStatus(Long id, String newStatus) {
        Account account = findAccountOrThrow(id);
        String status = newStatus.toUpperCase();

        validateStatusTransition(account, status);

        account.setStatus(status);
        Account saved = accountRepository.save(account);
        return mapToResponseDTO(saved);
    }

    private Account findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cuenta con ID " + id + " no encontrada"));
    }

    private void validateAccountType(String accountType) {
        if (!ACCOUNT_TYPE_AHORROS.equals(accountType) && !ACCOUNT_TYPE_CORRIENTE.equals(accountType)) {
            throw new IllegalArgumentException(
                    "El tipo de cuenta debe ser AHORROS o CORRIENTE");
        }
    }

    private void validateStatusTransition(Account account, String newStatus) {
        if (!STATUS_ACTIVA.equals(newStatus) && !STATUS_INACTIVA.equals(newStatus) && !STATUS_CANCELADA.equals(newStatus)) {
            throw new IllegalArgumentException(
                    "El estado debe ser ACTIVA, INACTIVA o CANCELADA");
        }

        if (STATUS_CANCELADA.equals(newStatus) && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Solo se pueden cancelar cuentas con saldo igual a $0. Saldo actual: $" + account.getBalance());
        }
    }

    private String generateUniqueAccountNumber(String accountType) {
        String prefix = ACCOUNT_TYPE_AHORROS.equals(accountType) ? "53" : "33";
        Random random = new Random();
        String accountNumber;

        do {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 8; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountResponseDTO mapToResponseDTO(Account account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountType(account.getAccountType())
                .accountNumber(account.getAccountNumber())
                .status(account.getStatus())
                .balance(account.getBalance())
                .gmfExempt(account.isGmfExempt())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .clientId(account.getClient().getId())
                .clientName(account.getClient().getFirstName() + " " + account.getClient().getLastName())
                .build();
    }
}
