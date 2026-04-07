package com.enterprise.ong_pet2.model.event;

import java.time.LocalDateTime;

public record AdocaoCriadaEvent(
        Long pedidoId,
        Long adotanteId,
        Long animalId,
        Long voluntarioId,
        Integer scoreMatching,
        LocalDateTime dataCriacao
) {}