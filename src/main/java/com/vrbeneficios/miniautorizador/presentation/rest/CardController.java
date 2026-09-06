package com.vrbeneficios.miniautorizador.presentation.rest;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vrbeneficios.miniautorizador.application.dto.CardInputDTO;
import com.vrbeneficios.miniautorizador.application.dto.CardOutputDTO;
import com.vrbeneficios.miniautorizador.application.usecase.CreateCardUseCase;
import com.vrbeneficios.miniautorizador.application.usecase.GetCardBalanceUseCase;
import com.vrbeneficios.miniautorizador.domain.exception.CardAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CardController {

    private final CreateCardUseCase createCardUseCase;
    private final GetCardBalanceUseCase getCardBalanceUseCase;

    @PostMapping
    public ResponseEntity<CardOutputDTO> create(@RequestBody CardInputDTO cardInputDTO) {
        CardOutputDTO output = createCardUseCase.execute(cardInputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String numeroCartao) {
        BigDecimal balance = getCardBalanceUseCase.execute(numeroCartao);
        if (balance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(balance);
    }

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<CardOutputDTO> handleCardAlreadyExists(CardAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new CardOutputDTO(ex.getCardNumber(), ex.getPassword()));
    }

}
