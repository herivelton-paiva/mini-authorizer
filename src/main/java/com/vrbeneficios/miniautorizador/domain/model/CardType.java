package com.vrbeneficios.miniautorizador.domain.model;

public enum CardType {
    VR("Vale Refeição"),
    VA("Vale Alimentação");

    private final String description;

    CardType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
