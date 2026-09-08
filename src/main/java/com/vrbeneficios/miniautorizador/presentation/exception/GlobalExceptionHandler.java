package com.vrbeneficios.miniautorizador.presentation.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vrbeneficios.miniautorizador.application.dto.CardOutputDTO;
import com.vrbeneficios.miniautorizador.domain.exception.CardAlreadyExistsException;
import com.vrbeneficios.miniautorizador.domain.exception.TransactionDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<CardOutputDTO> handleCardAlreadyExists(CardAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new CardOutputDTO(ex.getCardNumber(), ex.getPassword()));
    }

    @ExceptionHandler(TransactionDeniedException.class)
    public ResponseEntity<String> handleTransactionDenied(TransactionDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ex.getDenialReason().name());
    }
}
