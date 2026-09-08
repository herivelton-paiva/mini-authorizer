package com.vrbeneficios.miniautorizador.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "cardNumber")
public class Card {
    private Long id;
    private String cardNumber;
    private String password;
    private BigDecimal balance;
    private CardType type;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Card(String cardNumber, String password) {
        this(cardNumber, password, null);
    }

    public Card(String cardNumber, String password, CardType type) {
        this.cardNumber = cardNumber;
        this.password = password;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }
}