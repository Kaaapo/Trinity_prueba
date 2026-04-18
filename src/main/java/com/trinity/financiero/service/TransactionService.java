package com.trinity.financiero.service;

import com.trinity.financiero.dto.TransactionDTO;
import com.trinity.financiero.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {

    TransactionResponseDTO createTransaction(TransactionDTO dto);

    List<TransactionResponseDTO> getTransactionsByAccountId(Long accountId);
}
