package com.vrbeneficios.miniautorizador.application.usecase;

import org.springframework.stereotype.Service;

import com.vrbeneficios.miniautorizador.application.dto.CardInputDTO;
import com.vrbeneficios.miniautorizador.application.dto.CardOutputDTO;
import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.exception.CardAlreadyExistsException;
import com.vrbeneficios.miniautorizador.domain.model.Card;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCardUseCase {

    private final CardRepository cardRepository;

    public CardOutputDTO execute(CardInputDTO cardInputDTO) {

        var existingCard = cardRepository.findByCardNumber(cardInputDTO.numeroCartao());
        if (existingCard != null) {
            throw new CardAlreadyExistsException(cardInputDTO.numeroCartao(), cardInputDTO.senha());
        }
        var card = cardInputDTO.toDomain();
        cardRepository.save(card);
        return CardOutputDTO.from(card);
    }

}
