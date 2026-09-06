package com.vrbeneficios.miniautorizador.application.dto;

import com.vrbeneficios.miniautorizador.domain.model.Card;

public record CardOutputDTO(
    String numeroCartao,
    String senha
) {

    public static CardOutputDTO from(Card card) {
        if (card == null) {
            return null;
        }
        return new CardOutputDTO(card.getCardNumber(), card.getPassword());
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public String getSenha() {
        return senha;
    }
}
