package com.enterprise.ong_pet2.model.dto.estoque;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.enums.UnidadeMedida;

import java.math.BigDecimal;

public record ItemEstoqueResponseDTO(
        Long id,
        String nome,
        CategoriaEstoque categoria,
        UnidadeMedida unidadeMedida,
        BigDecimal quantidadeAtual,
        BigDecimal quantidadeMinima,
        String descricao,
        Boolean ativo,
        Boolean estoqueBaixo
) {}
