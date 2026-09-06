package com.vrbeneficios.miniautorizador.application.dto;

import com.vrbeneficios.miniautorizador.domain.model.Card;

public record CardInputDTO(
        String numeroCartao,
        String senha) {

    public CardInputDTO {
        if (numeroCartao == null || numeroCartao.isBlank()
                || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Número do cartão e senha são obrigatórios.");
        }
    }

    public Card toDomain() {
        return new Card(numeroCartao, senha);
    }

    public String getNumeroCartao() {
        return numeroCartao;
    }

    public String getSenha() {
        return senha;
    }
}
