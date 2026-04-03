package com.enterprise.ong_pet2.dto.estoque;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.enums.UnidadeMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ItemEstoqueRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaEstoque categoria,

        @NotNull(message = "Unidade de medida é obrigatória")
        UnidadeMedida unidadeMedida,

        @PositiveOrZero(message = "Quantidade mínima deve ser zero ou positiva")
        BigDecimal quantidadeMinima,

        String descricao
) {}
