package com.vrbeneficios.miniautorizador.domain.rule;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.inOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.domain.model.Card;

@ExtendWith(MockitoExtension.class)
class AuthorizationEngineTest {

    @Test
    @DisplayName("AuthorizationEngine deve executar todas as regras na ordem correta")
    void shouldExecuteRulesInOrder() {
        var rule1 = Mockito.mock(AuthorizationRule.class);
        var rule2 = Mockito.mock(AuthorizationRule.class);
        var rule3 = Mockito.mock(AuthorizationRule.class);

        var engine = new AuthorizationEngine(List.of(rule1, rule2, rule3));

        var card = Card.builder().cardNumber("1234567890123456").build();
        var input = new TransactionInputDTO("1234567890123456", "1234", new BigDecimal("10.00"));

        assertDoesNotThrow(() -> engine.process(card, input));

        var inOrder = inOrder(rule1, rule2, rule3);
        inOrder.verify(rule1).validate(card, input);
        inOrder.verify(rule2).validate(card, input);
        inOrder.verify(rule3).validate(card, input);
    }
}
