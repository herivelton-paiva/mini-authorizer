package com.vrbeneficios.miniautorizador.presentation.rest;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CardController {

    private final CreateCardUseCase createCardUseCase;
    private final GetCardBalanceUseCase getCardBalanceUseCase;

    @PostMapping
    public ResponseEntity<CardOutputDTO> create(@Valid @RequestBody CardInputDTO cardInputDTO) {
        createCardUseCase.execute(cardInputDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CardOutputDTO(cardInputDTO.numeroCartao(), cardInputDTO.senha()));
    }

    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String numeroCartao) {
        return Optional.ofNullable(getCardBalanceUseCase.execute(numeroCartao))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
