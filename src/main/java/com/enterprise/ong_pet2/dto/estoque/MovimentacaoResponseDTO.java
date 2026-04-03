package com.enterprise.ong_pet2.dto.estoque;

import com.enterprise.ong_pet2.enums.MotivoMovimentacao;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponseDTO(
        Long id,
        TipoMovimentacao tipo,
        BigDecimal quantidade,
        LocalDateTime dataMovimentacao,
        MotivoMovimentacao motivo,
        String observacoes,
        String responsavelNome
) {}
