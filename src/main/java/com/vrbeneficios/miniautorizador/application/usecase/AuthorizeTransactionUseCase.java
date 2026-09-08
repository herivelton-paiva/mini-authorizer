package com.vrbeneficios.miniautorizador.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vrbeneficios.miniautorizador.application.dto.TransactionInputDTO;
import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.exception.TransactionDeniedException;
import com.vrbeneficios.miniautorizador.domain.model.Card;
import com.vrbeneficios.miniautorizador.domain.model.Transaction;
import com.vrbeneficios.miniautorizador.domain.model.TransactionDenialReasonEnum;
import com.vrbeneficios.miniautorizador.domain.model.TransactionStatusEnum;
import com.vrbeneficios.miniautorizador.domain.rule.AuthorizationEngine;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizeTransactionUseCase {

    private final CardRepository cardRepository;
    private final AuthorizationEngine authorizationEngine;

    @Transactional
    public void execute(TransactionInputDTO input) {
        var card = cardRepository.findByCardNumberWithLock(input.numeroCartao());

        try {
            authorizationEngine.process(card, input);

            debit(card, input.valor());
            cardRepository.save(card);

            createTransaction(card, input, TransactionStatusEnum.AUTHORIZED, null);
            // TODO: Persist transaction

        } catch (TransactionDeniedException ex) {
            createTransaction(card, input, TransactionStatusEnum.DENIED, ex.getDenialReason());
            // TODO: Persist transaction

            throw ex;
        }
    }

    private Transaction createTransaction(final Card card, final TransactionInputDTO input, final TransactionStatusEnum status,
            final TransactionDenialReasonEnum denialReason) {
        return Transaction.builder()
                .card(card)
                .cardNumber(input.numeroCartao())
                .amount(input.valor())
                .status(status)
                .denialReason(denialReason)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void debit(Card card, BigDecimal amount) {
        card.setBalance(card.getBalance().subtract(amount));
        card.setUpdatedAt(LocalDateTime.now());
    }
}
