package com.enterprise.ong_pet2.model.event;

import com.enterprise.ong_pet2.enums.TipoDoacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DoacaoCriadaEvent(
        Long doacaoId,
        Long doadorId,
        BigDecimal valor,
        TipoDoacao categoria,
        LocalDateTime dataCriacao
) {}