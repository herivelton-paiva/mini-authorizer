package com.vrbeneficios.miniautorizador.domain.exception;

import lombok.Getter;

@Getter
public class CardAlreadyExistsException extends RuntimeException {

    private final String cardNumber;

    public CardAlreadyExistsException(String cardNumber) {
        super("Cartão já existe");
        this.cardNumber = cardNumber;
    }

}
