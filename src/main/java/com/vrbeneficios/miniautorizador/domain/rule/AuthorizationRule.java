package com.vrbeneficios.miniautorizador.domain.rule;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.domain.model.Card;

public interface AuthorizationRule {
    void validate(Card card, TransactionInputDTO transaction);
}
