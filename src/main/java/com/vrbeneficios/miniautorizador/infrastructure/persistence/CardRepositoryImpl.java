package com.vrbeneficios.miniautorizador.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vrbeneficios.miniautorizador.application.repository.CardRepository;
import com.vrbeneficios.miniautorizador.domain.model.Card;

@Repository
public interface CardRepositoryImpl extends JpaRepository<CardEntity, Long>, CardRepository {

    Optional<CardEntity> findFirstByCardNumber(String cardNumber);

    @Override
    default Card findByCardNumber(String cardNumber) {
        return findFirstByCardNumber(cardNumber)
                .map(CardEntity::toDomain)
                .orElse(null);
    }

    @Override
    default void save(Card card) {
        save(CardEntity.fromDomain(card));
    }
}