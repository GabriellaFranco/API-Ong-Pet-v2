package com.enterprise.ong_pet2.model.dto.perfil_adotante;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.FaixaRenda;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.enums.TipoMoradia;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PerfilAdotanteRequestDTO(
        @NotNull(message = "Tipo de moradia é obrigatório")
        TipoMoradia tipoMoradia,

        Integer areaM2,

        @NotNull(message = "Informe se possui outros pets")
        Boolean temOutrosPets,

        String descricaoOutrosPets,

        @NotBlank(message = "Rotina diária é obrigatória")
        String rotinaDiaria,

        @NotNull(message = "Horas em casa por dia é obrigatório")
        @Min(value = 0, message = "Valor mínimo é 0")
        @Max(value = 24, message = "Valor máximo é 24")
        Integer horasEmCasaPorDia,

        @NotNull(message = "Faixa de renda é obrigatória")
        FaixaRenda rendaMensalFaixa,

        @NotNull(message = "Informe se tem experiência com animais")
        Boolean experienciaAnimais,

        @NotBlank(message = "Motivação para adoção é obrigatória")
        String motivacaoAdocao,

        Especie especiePreferida,

        PorteAnimal portePreferido,

        @NotNull(message = "Informe se aceita crianças")
        Boolean aceitaCriancas
) {}
