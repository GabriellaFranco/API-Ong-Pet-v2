package com.enterprise.ong_pet2.dto.doacao;

import com.enterprise.ong_pet2.enums.TipoDoacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DoacaoRequestDTO(
        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "Categoria é obrigatória")
        TipoDoacao categoria,

        String descricao,

        Long idAnimal
) {}