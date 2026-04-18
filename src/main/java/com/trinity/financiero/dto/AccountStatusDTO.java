package com.trinity.financiero.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusDTO {

    @NotBlank(message = "El estado es obligatorio (ACTIVA, INACTIVA, CANCELADA)")
    private String status;
}
