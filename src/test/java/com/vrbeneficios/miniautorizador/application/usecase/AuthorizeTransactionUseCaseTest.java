package com.vrbeneficios.miniautorizador.application.usecase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.exception.TransactionDeniedException;
import com.vrbeneficios.miniautorizador.domain.model.Card;
import com.vrbeneficios.miniautorizador.domain.model.TransactionDenialReasonEnum;
import com.vrbeneficios.miniautorizador.domain.rule.AuthorizationEngine;

@ExtendWith(MockitoExtension.class)
class AuthorizeTransactionUseCaseTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private AuthorizationEngine authorizationEngine;

    @InjectMocks
    private AuthorizeTransactionUseCase authorizeTransactionUseCase;

    @Test
    @DisplayName("Deve autorizar transação com sucesso e debitar saldo do cartão")
    void shouldAuthorizeTransactionSuccessfully() {
        var cardNumber = "6549873025634501";
        var card = Card.builder()
                .cardNumber(cardNumber)
                .password("1234")
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumberWithLock(cardNumber)).thenReturn(card);

        var input = new TransactionInputDTO(cardNumber, "1234", new BigDecimal("10.00"));

        authorizeTransactionUseCase.execute(input);

        assertEquals(new BigDecimal("490.00"), card.getBalance());
        verify(cardRepository).save(card);
    }

    @Test
    @DisplayName("Deve negar transação quando o cartão não existir (CARTAO_INEXISTENTE)")
    void shouldDenyWhenCardDoesNotExist() {
        var cardNumber = "9999999999999999";
        when(cardRepository.findByCardNumberWithLock(cardNumber)).thenReturn(null);

        var input = new TransactionInputDTO(cardNumber, "1234", new BigDecimal("10.00"));
        doThrow(new TransactionDeniedException(TransactionDenialReasonEnum.CARTAO_INEXISTENTE))
                .when(authorizationEngine).process(null, input);

        var ex = assertThrows(
                TransactionDeniedException.class,
                () -> authorizeTransactionUseCase.execute(input));

        assertEquals(TransactionDenialReasonEnum.CARTAO_INEXISTENTE, ex.getDenialReason());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve negar transação quando a senha for inválida (SENHA_INVALIDA)")
    void shouldDenyWhenPasswordIsInvalid() {
        var cardNumber = "6549873025634501";
        var card = Card.builder()
                .cardNumber(cardNumber)
                .password("1234")
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumberWithLock(cardNumber)).thenReturn(card);

        var input = new TransactionInputDTO(cardNumber, "9999", new BigDecimal("10.00"));
        doThrow(new TransactionDeniedException(TransactionDenialReasonEnum.SENHA_INVALIDA))
                .when(authorizationEngine).process(card, input);

        var ex = assertThrows(
                TransactionDeniedException.class,
                () -> authorizeTransactionUseCase.execute(input));

        assertEquals(TransactionDenialReasonEnum.SENHA_INVALIDA, ex.getDenialReason());
        assertEquals(new BigDecimal("500.00"), card.getBalance());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve negar transação quando saldo for insuficiente (SALDO_INSUFICIENTE)")
    void shouldDenyWhenBalanceIsInsufficient() {
        var cardNumber = "6549873025634501";
        var card = Card.builder()
                .cardNumber(cardNumber)
                .password("1234")
                .balance(new BigDecimal("5.00"))
                .build();

        when(cardRepository.findByCardNumberWithLock(cardNumber)).thenReturn(card);

        var input = new TransactionInputDTO(cardNumber, "1234", new BigDecimal("10.00"));
        doThrow(new TransactionDeniedException(TransactionDenialReasonEnum.SALDO_INSUFICIENTE))
                .when(authorizationEngine).process(card, input);

        var ex = assertThrows(
                TransactionDeniedException.class,
                () -> authorizeTransactionUseCase.execute(input));

        assertEquals(TransactionDenialReasonEnum.SALDO_INSUFICIENTE, ex.getDenialReason());
        assertEquals(new BigDecimal("5.00"), card.getBalance());
        verify(cardRepository, never()).save(any());
    }
}
