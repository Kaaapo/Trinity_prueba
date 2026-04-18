package com.trinity.financiero.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private Long id;
    private String transactionType;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
    private Long accountId;
    private String accountNumber;
    private Long destinationAccountId;
    private String destinationAccountNumber;
}
