package com.enterprise.ong_pet2.dto.animal;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.Genero;
import com.enterprise.ong_pet2.enums.PorteAnimal;

public record AnimalResponseDTO(
        Long id,
        String nome,
        Especie especie,
        Long idade,
        Genero genero,
        PorteAnimal porte,
        String descricao,
        Boolean disponivel,
        Boolean vacinado,
        Boolean castrado,
        Boolean microchipado,
        ResponsavelDTO responsavel
) {
    public record ResponsavelDTO(Long id, String nome) {}
}
