package com.vrbeneficios.miniautorizador.application.dto;

import java.util.Optional;

import com.vrbeneficios.miniautorizador.domain.model.Card;

public record CardOutputDTO(
        String numeroCartao,
        String senha) {

    public static CardOutputDTO from(Card card) {
        return Optional.ofNullable(card)
                .map(c -> new CardOutputDTO(c.getCardNumber(), c.getPassword()))
                .orElse(null);
    }
}
