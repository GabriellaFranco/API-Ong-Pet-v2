package com.enterprise.ong_pet2.model.dto.despesa;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DespesaResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        CategoriaEstoque categoria,
        String observacoes,
        AnimalDTO animal,
        ResponsavelDTO responsavel
) {
    public record AnimalDTO(Long id, String nome) {}
    public record ResponsavelDTO(Long id, String nome) {}
}
