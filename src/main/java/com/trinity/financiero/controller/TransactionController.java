package com.trinity.financiero.controller;

import com.trinity.financiero.dto.TransactionDTO;
import com.trinity.financiero.dto.TransactionResponseDTO;
import com.trinity.financiero.service.TransactionService;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Operaciones financieras e historial de movimientos")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Registrar una transacción", description = "Registra una consignación, retiro o transferencia entre cuentas.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transacción registrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o saldo insuficiente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "La operación no puede realizarse", content = @Content)
    })
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionResponseDTO response = transactionService.createTransaction(transactionDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Consultar movimientos de una cuenta", description = "Obtiene el historial de transacciones asociado a una cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de transacciones"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada", content = @Content)
    })
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByAccountId(@PathVariable Long accountId) {
        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }
}
