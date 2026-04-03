package com.enterprise.ong_pet2.dto.animal;

public record AnimalUpdateDTO(
        Boolean disponivel,
        Boolean vacinado,
        Boolean castrado,
        Boolean microchipado,
        String descricao
) {}
