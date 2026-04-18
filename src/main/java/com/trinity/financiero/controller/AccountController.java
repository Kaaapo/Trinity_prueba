package com.trinity.financiero.controller;

import com.trinity.financiero.dto.AccountDTO;
import com.trinity.financiero.dto.AccountResponseDTO;
import com.trinity.financiero.dto.AccountStatusDTO;
import com.trinity.financiero.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        AccountResponseDTO response = accountService.createAccount(accountDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(@PathVariable Long id) {
        AccountResponseDTO response = accountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByClientId(@PathVariable Long clientId) {
        List<AccountResponseDTO> accounts = accountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody AccountStatusDTO statusDTO) {
        AccountResponseDTO response = accountService.updateAccountStatus(id, statusDTO.getStatus());
        return ResponseEntity.ok(response);
    }
}
