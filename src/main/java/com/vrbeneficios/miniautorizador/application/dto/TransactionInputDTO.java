package com.vrbeneficios.miniautorizador.application.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionInputDTO(
        @NotBlank(message = "Número do cartão é obrigatório.") String numeroCartao,
        @NotBlank(message = "Senha do cartão é obrigatória.") String senhaCartao,
        @NotNull(message = "Valor da transação é obrigatório.") @Positive(message = "Valor da transação deve ser positivo.") BigDecimal valor) {
}
