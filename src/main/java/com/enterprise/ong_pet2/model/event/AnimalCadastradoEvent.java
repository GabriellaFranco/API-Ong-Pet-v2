package com.enterprise.ong_pet2.model.event;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.PorteAnimal;

import java.time.LocalDateTime;

public record AnimalCadastradoEvent(
        Long animalId,
        Long responsavelId,
        String nome,
        Especie especie,
        PorteAnimal porte,
        LocalDateTime dataCadastro
) {}