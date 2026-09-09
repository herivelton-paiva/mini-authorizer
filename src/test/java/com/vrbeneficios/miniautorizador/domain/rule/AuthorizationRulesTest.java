package com.vrbeneficios.miniautorizador.domain.rule;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.domain.exception.TransactionDeniedException;
import com.vrbeneficios.miniautorizador.domain.model.Card;
import com.vrbeneficios.miniautorizador.domain.model.TransactionDenialReasonEnum;

@ExtendWith(MockitoExtension.class)
class AuthorizationRulesTest {

    @InjectMocks
    private CardExistenceRule existenceRule;

    @InjectMocks
    private CardPasswordRule passwordRule;

    @InjectMocks
    private CardBalanceRule balanceRule;

    @Mock
    private Card card;

    @Mock
    private TransactionInputDTO input;

    @Test
    @DisplayName("CardExistenceRule deve lançar CARTAO_INEXISTENTE se cartão for nulo")
    void shouldThrowWhenCardIsNull() {
        var ex = assertThrows(
                TransactionDeniedException.class,
                () -> existenceRule.validate(null, input)
        );

        assertEquals(TransactionDenialReasonEnum.CARTAO_INEXISTENTE, ex.getDenialReason());
    }

    @Test
    @DisplayName("CardExistenceRule não deve lançar exceção se cartão existir")
    void shouldPassWhenCardExists() {
        assertDoesNotThrow(() -> existenceRule.validate(card, input));
    }

    @Test
    @DisplayName("CardPasswordRule deve lançar SENHA_INVALIDA se senha for incorreta")
    void shouldThrowWhenPasswordIsIncorrect() {
        when(card.getPassword()).thenReturn("1234");
        when(input.senhaCartao()).thenReturn("0000");

        var ex = assertThrows(
                TransactionDeniedException.class,
                () -> passwordRule.validate(card, input)
        );

        assertEquals(TransactionDenialReasonEnum.SENHA_INVALIDA, ex.getDenialReason());
    }

    @Test
    @DisplayName("CardPasswordRule não deve lançar exceção se senha for correta")
    void shouldPassWhenPasswordIsCorrect() {
        when(card.getPassword()).thenReturn("1234");
        when(input.senhaCartao()).thenReturn("1234");

        assertDoesNotThrow(() -> passwordRule.validate(card, input));
    }

    @Test
    @DisplayName("CardBalanceRule deve lançar SALDO_INSUFICIENTE se saldo for menor que o valor")
    void shouldThrowWhenBalanceIsInsufficient() {
        when(card.getBalance()).thenReturn(new BigDecimal("5.00"));
        when(input.valor()).thenReturn(new BigDecimal("10.00"));

        var ex = assertThrows(
                TransactionDeniedException.class,
                () -> balanceRule.validate(card, input)
        );

        assertEquals(TransactionDenialReasonEnum.SALDO_INSUFICIENTE, ex.getDenialReason());
    }

    @Test
    @DisplayName("CardBalanceRule não deve lançar exceção se saldo for suficiente")
    void shouldPassWhenBalanceIsSufficient() {
        when(card.getBalance()).thenReturn(new BigDecimal("10.00"));
        when(input.valor()).thenReturn(new BigDecimal("10.00"));

        assertDoesNotThrow(() -> balanceRule.validate(card, input));
    }
}
