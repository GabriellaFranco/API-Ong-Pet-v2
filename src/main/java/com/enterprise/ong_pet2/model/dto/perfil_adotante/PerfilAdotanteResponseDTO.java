package com.enterprise.ong_pet2.model.dto.perfil_adotante;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.FaixaRenda;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.enums.TipoMoradia;

public record PerfilAdotanteResponseDTO(
        Long id,
        TipoMoradia tipoMoradia,
        Integer areaM2,
        Boolean temOutrosPets,
        String descricaoOutrosPets,
        String rotinaDiaria,
        Integer horasEmCasaPorDia,
        FaixaRenda rendaMensalFaixa,
        Boolean experienciaAnimais,
        String motivacaoAdocao,
        Especie especiePreferida,
        PorteAnimal portePreferido,
        Boolean aceitaCriancas,
        Integer scoreRisco
) {}