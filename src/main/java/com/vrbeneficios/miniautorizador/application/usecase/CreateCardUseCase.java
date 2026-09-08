package com.vrbeneficios.miniautorizador.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vrbeneficios.miniautorizador.application.dto.CardInputDTO;
import com.vrbeneficios.miniautorizador.application.dto.CardOutputDTO;
import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.exception.CardAlreadyExistsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCardUseCase {

    private final CardRepository cardRepository;

    public CardOutputDTO execute(CardInputDTO cardInputDTO) {
        return Optional.of(cardInputDTO)
                .filter(dto -> cardRepository.findByCardNumber(dto.numeroCartao()) == null)
                .map(CardInputDTO::toDomain)
                .map(card -> {
                    cardRepository.save(card);
                    return CardOutputDTO.from(card);
                })
                .orElseThrow(() -> new CardAlreadyExistsException(cardInputDTO.numeroCartao(), cardInputDTO.senha()));
    }

}
