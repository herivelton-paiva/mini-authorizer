package com.vrbeneficios.miniautorizador.application.usecase;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.model.Card;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCardBalanceUseCase {

    private final CardRepository cardRepository;

    public BigDecimal execute(String cardNumber) {
        var card = cardRepository.findByCardNumber(cardNumber);
        return Optional.ofNullable(card)
                .map(Card::getBalance)
                .orElse(null);
    }

}
