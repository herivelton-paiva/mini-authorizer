package com.vrbeneficios.miniautorizador.application.dto;

import com.vrbeneficios.miniautorizador.domain.model.Card;
import jakarta.validation.constraints.NotBlank;

public record CardInputDTO(
        @NotBlank(message = "Número do cartão é obrigatório.") String numeroCartao,
        @NotBlank(message = "Senha do cartão é obrigatória.") String senha) {

    public Card toDomain() {
        return new Card(numeroCartao, senha);
    }
}
