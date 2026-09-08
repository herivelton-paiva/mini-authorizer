package com.vrbeneficios.miniautorizador.application.repository;

import com.vrbeneficios.miniautorizador.domain.model.Card;

public interface CardRepository {
    Card findByCardNumber(String cardNumber);

    Card findByCardNumberWithLock(String cardNumber);

    void save(Card card);
}
