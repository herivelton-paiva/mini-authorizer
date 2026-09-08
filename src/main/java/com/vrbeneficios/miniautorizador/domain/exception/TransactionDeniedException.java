package com.vrbeneficios.miniautorizador.domain.exception;

import com.vrbeneficios.miniautorizador.domain.model.TransactionDenialReasonEnum;

import lombok.Getter;

@Getter
public class TransactionDeniedException extends RuntimeException {

    private final TransactionDenialReasonEnum denialReason;

    public TransactionDeniedException(TransactionDenialReasonEnum denialReason) {
        super(denialReason.name());
        this.denialReason = denialReason;
    }
}
