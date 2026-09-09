package com.vrbeneficios.miniautorizador.application.usecase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.model.Card;

@ExtendWith(MockitoExtension.class)
class GetCardBalanceUseCaseTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private GetCardBalanceUseCase getCardBalanceUseCase;

    @Test
    @DisplayName("Deve retornar o saldo do cartão quando ele existir")
    void shouldReturnBalanceWhenCardExists() {
        var cardNumber = "6549873025634501";
        var expectedBalance = new BigDecimal("500.00");
        var card = Card.builder()
                .cardNumber(cardNumber)
                .password("1234")
                .balance(expectedBalance)
                .build();

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(card);

        var balance = getCardBalanceUseCase.execute(cardNumber);

        assertEquals(expectedBalance, balance);
        verify(cardRepository).findByCardNumber(cardNumber);
    }

    @Test
    @DisplayName("Deve retornar null quando o cartão não existir")
    void shouldReturnNullWhenCardDoesNotExist() {
        var cardNumber = "9999999999999999";
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(null);

        var balance = getCardBalanceUseCase.execute(cardNumber);

        assertNull(balance);
        verify(cardRepository).findByCardNumber(cardNumber);
    }
}
