package com.trinity.financiero.controller;

import com.trinity.financiero.dto.AccountDTO;
import com.trinity.financiero.dto.AccountResponseDTO;
import com.trinity.financiero.dto.AccountStatusDTO;
import com.trinity.financiero.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Administración de cuentas bancarias")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Crear una cuenta", description = "Crea una cuenta de ahorros o corriente para un cliente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        AccountResponseDTO response = accountService.createAccount(accountDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una cuenta", description = "Obtiene el detalle y saldo de una cuenta por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        AccountResponseDTO response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar cuentas de un cliente", description = "Obtiene las cuentas asociadas al cliente indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de cuentas"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content)
    })
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByClientId(@PathVariable Long clientId) {
        List<AccountResponseDTO> accounts = accountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de una cuenta", description = "Actualiza el estado de la cuenta a ACTIVA, INACTIVA o CANCELADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody AccountStatusDTO statusDTO) {
        AccountResponseDTO response = accountService.updateAccountStatus(id, statusDTO.getStatus());
        return ResponseEntity.ok(response);
    }
}
