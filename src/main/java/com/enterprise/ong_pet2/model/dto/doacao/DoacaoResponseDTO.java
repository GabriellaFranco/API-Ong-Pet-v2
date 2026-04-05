package com.enterprise.ong_pet2.model.dto.doacao;

import com.enterprise.ong_pet2.enums.TipoDoacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DoacaoResponseDTO(
        Long id,
        BigDecimal valor,
        TipoDoacao categoria,
        String descricao,
        LocalDateTime dataDoacao,
        DoadorDTO doador,
        AnimalDTO animal
) {
    public record DoadorDTO(Long id, String nome) {}
    public record AnimalDTO(Long id, String nome) {}
}