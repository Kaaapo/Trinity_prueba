package com.trinity.financiero.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {

    @NotBlank(message = "El tipo de cuenta es obligatorio (AHORROS o CORRIENTE)")
    private String accountType;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clientId;

    private boolean gmfExempt;
}
