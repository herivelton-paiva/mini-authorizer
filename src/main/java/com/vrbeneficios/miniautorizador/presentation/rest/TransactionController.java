package com.vrbeneficios.miniautorizador.presentation.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.application.usecase.AuthorizeTransactionUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransactionController {

    private final AuthorizeTransactionUseCase authorizeTransactionUseCase;

    @PostMapping
    public ResponseEntity<String> authorize(@Valid @RequestBody TransactionInputDTO transactionInputDTO) {
        authorizeTransactionUseCase.execute(transactionInputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
}
