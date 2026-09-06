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
@EqualsAndHashCode(of = "id")
public class Transaction {

    private Long id;
    private Card card;
    private String cardNumber;
    private BigDecimal amount;
    private TransactionStatusEnum status;
    private TransactionDenialReasonEnum denialReason;
    private LocalDateTime createdAt;

}
