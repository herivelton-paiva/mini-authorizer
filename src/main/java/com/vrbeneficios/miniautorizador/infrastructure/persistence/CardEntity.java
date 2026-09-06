package com.vrbeneficios.miniautorizador.infrastructure.persistence;

import com.vrbeneficios.miniautorizador.domain.model.Card;
import com.vrbeneficios.miniautorizador.domain.model.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "cardNumber")
public class CardEntity {

    public static final BigDecimal INITIAL_BALANCE = new BigDecimal("500.00");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", unique = true, nullable = false, length = 16)
    private String cardNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = INITIAL_BALANCE;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", length = 10)
    private CardType type;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) {
            this.balance = INITIAL_BALANCE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Card toDomain() {
        return Card.builder()
                .id(this.id)
                .cardNumber(this.cardNumber)
                .password(this.password)
                .balance(this.balance)
                .type(this.type)
                .version(this.version)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    public static CardEntity fromDomain(Card card) {
        if (card == null) {
            return null;
        }
        return CardEntity.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .password(card.getPassword())
                .balance(card.getBalance())
                .type(card.getType())
                .version(card.getVersion())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }

    public static CardEntity toEntity(Card card) {
        return fromDomain(card);
    }
}
