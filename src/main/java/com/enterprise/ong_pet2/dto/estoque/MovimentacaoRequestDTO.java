package com.enterprise.ong_pet2.dto.estoque;

import com.enterprise.ong_pet2.enums.MotivoMovimentacao;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record MovimentacaoRequestDTO(
        @NotNull(message = "Tipo é obrigatório")
        TipoMovimentacao tipo,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser positiva")
        BigDecimal quantidade,

        @NotNull(message = "Motivo é obrigatório")
        MotivoMovimentacao motivo,

        String observacoes,

        Long idDoacao,
        Long idAnimal
) {}