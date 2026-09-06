package com.vrbeneficios.miniautorizador.domain.exception;

import lombok.Getter;

@Getter
public class CardAlreadyExistsException extends RuntimeException {

    private final String cardNumber;
    private final String password;

    public CardAlreadyExistsException(String cardNumber, String password) {
        super("Cartão já existe");
        this.cardNumber = cardNumber;
        this.password = password;
    }

}
