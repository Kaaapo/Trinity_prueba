package com.trinity.financiero.service;

import com.trinity.financiero.dto.AccountDTO;
import com.trinity.financiero.dto.AccountResponseDTO;

import java.util.List;

public interface AccountService {

    AccountResponseDTO createAccount(AccountDTO dto);

    AccountResponseDTO getAccountById(Long id);

    List<AccountResponseDTO> getAccountsByClientId(Long clientId);

    AccountResponseDTO updateAccountStatus(Long id, String newStatus);
}
