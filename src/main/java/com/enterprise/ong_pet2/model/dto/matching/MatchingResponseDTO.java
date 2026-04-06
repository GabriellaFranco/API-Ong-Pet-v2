package com.enterprise.ong_pet2.model.dto.matching;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.PorteAnimal;

import java.util.List;

public record MatchingResponseDTO(
        Long animalId,
        String animalNome,
        Especie especie,
        PorteAnimal porte,
        Long adotanteId,
        String adotanteNome,
        int scoreTotal,
        ClassificacaoMatching classificacao,
        List<FatorScoreDTO> fatores
) {
    public enum ClassificacaoMatching {
        ALTA, MEDIA, BAIXA
    }
}