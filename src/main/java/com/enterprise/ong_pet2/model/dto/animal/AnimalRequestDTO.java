package com.enterprise.ong_pet2.model.dto.animal;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.Genero;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AnimalRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Espécie é obrigatória")
        Especie especie,

        @NotNull(message = "Idade é obrigatória")
        @Positive(message = "Idade deve ser positiva")
        Long idade,

        @NotNull(message = "Gênero é obrigatório")
        Genero genero,

        @NotNull(message = "Porte é obrigatório")
        PorteAnimal porte,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        Boolean vacinado,
        Boolean castrado,
        Boolean microchipado
) {}