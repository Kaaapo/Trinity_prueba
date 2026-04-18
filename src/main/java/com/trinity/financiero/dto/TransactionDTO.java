package com.trinity.financiero.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    @NotBlank(message = "El tipo de transacción es obligatorio (CONSIGNACION, RETIRO, TRANSFERENCIA)")
    private String transactionType;

    @NotNull(message = "El ID de la cuenta es obligatorio")
    private Long accountId;

    private Long destinationAccountId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    private String description;
}
