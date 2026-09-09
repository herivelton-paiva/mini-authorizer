package com.vrbeneficios.miniautorizador.application.usecase;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vrbeneficios.miniautorizador.application.dto.CardInputDTO;
import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.exception.CardAlreadyExistsException;
import com.vrbeneficios.miniautorizador.domain.model.Card;

@ExtendWith(MockitoExtension.class)
class CreateCardUseCaseTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CreateCardUseCase createCardUseCase;

    @Test
    @DisplayName("Deve criar cartão com sucesso quando ele não existir")
    void shouldCreateCardSuccessfully() {
        var cardNumber = "6549873025634501";
        var password = "1234";
        var input = new CardInputDTO(cardNumber, password);

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(null);

        var result = createCardUseCase.execute(input);

        assertNotNull(result);
        assertEquals(cardNumber, result.numeroCartao());
        assertEquals(password, result.senha());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Deve lançar CardAlreadyExistsException quando cartão já existir")
    void shouldThrowExceptionWhenCardAlreadyExists() {
        var cardNumber = "6549873025634501";
        var password = "1234";
        var input = new CardInputDTO(cardNumber, password);

        var existingCard = Card.builder()
                .cardNumber(cardNumber)
                .password("1234")
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(existingCard);

        var ex = assertThrows(
                CardAlreadyExistsException.class,
                () -> createCardUseCase.execute(input));

        assertEquals(cardNumber, ex.getCardNumber());
        assertEquals(password, ex.getPassword());
        verify(cardRepository, never()).save(any());
    }
}
