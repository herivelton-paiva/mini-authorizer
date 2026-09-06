package com.vrbeneficios.miniautorizador.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.model.Card;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCardBalanceUseCase {

    private final CardRepository cardRepository;

    public BigDecimal execute(String cardNumber) {
        Card card = cardRepository.findByCardNumber(cardNumber);
        if (card == null) {
            return null;
        }
        return card.getBalance();
    }

}
