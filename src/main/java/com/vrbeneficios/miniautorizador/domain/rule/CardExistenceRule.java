package com.vrbeneficios.miniautorizador.domain.rule;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.domain.exception.TransactionDeniedException;
import com.vrbeneficios.miniautorizador.domain.model.Card;
import com.vrbeneficios.miniautorizador.domain.model.TransactionDenialReasonEnum;

@Component
@Order(1)
public class CardExistenceRule implements AuthorizationRule {

    @Override
    public void validate(Card card, TransactionInputDTO transaction) {
        Optional.ofNullable(card)
                .orElseThrow(() -> new TransactionDeniedException(TransactionDenialReasonEnum.CARTAO_INEXISTENTE));
    }
}
