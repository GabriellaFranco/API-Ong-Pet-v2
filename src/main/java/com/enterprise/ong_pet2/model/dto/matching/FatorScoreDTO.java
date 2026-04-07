package com.enterprise.ong_pet2.model.dto.matching;

public record FatorScoreDTO(
        String nome,
        int pontos,
        int pontosPossiveis,
        String descricao
) {}