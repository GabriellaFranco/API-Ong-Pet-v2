package com.enterprise.ong_pet2.model.event;

import com.enterprise.ong_pet2.enums.StatusAdocao;

import java.time.LocalDateTime;

public record AdocaoStatusChangedEvent(
        Long pedidoId,
        Long adotanteId,
        Long animalId,
        Long voluntarioId,
        StatusAdocao statusAnterior,
        StatusAdocao statusNovo,
        LocalDateTime dataAlteracao
) {}