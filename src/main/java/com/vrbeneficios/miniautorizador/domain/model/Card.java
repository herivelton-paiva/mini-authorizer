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

    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    private Long id;
    private String cardNumber;
    private String password;

    @Builder.Default
    private BigDecimal balance = INITIAL_BALANCE;

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
        this.balance = INITIAL_BALANCE;
        this.createdAt = LocalDateTime.now();
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return amount != null && this.balance != null && this.balance.compareTo(amount) >= 0;
    }

    public void debit(BigDecimal amount) {
        if (!hasSufficientBalance(amount)) {
            throw new IllegalStateException("Saldo insuficiente para efetuar o débito");
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean matchesPassword(String rawPassword) {
        return this.password != null && this.password.equals(rawPassword);
    }
}
