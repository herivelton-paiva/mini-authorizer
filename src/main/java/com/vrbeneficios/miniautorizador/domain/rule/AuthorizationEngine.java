package com.vrbeneficios.miniautorizador.domain.rule;

import java.util.List;

import org.springframework.stereotype.Component;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.domain.model.Card;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationEngine {

    private final List<AuthorizationRule> rules;

    public void process(Card card, TransactionInputDTO transaction) {
        rules.forEach(rule -> rule.validate(card, transaction));
    }
}
