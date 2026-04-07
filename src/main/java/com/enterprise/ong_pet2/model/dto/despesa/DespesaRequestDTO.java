package com.enterprise.ong_pet2.model.dto.despesa;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaRequestDTO(
        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        @NotNull(message = "Valor é obrigatório")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @NotNull(message = "Data é obrigatória")
        LocalDate data,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaEstoque categoria,

        String observacoes,

        Long idAnimal
) {}
