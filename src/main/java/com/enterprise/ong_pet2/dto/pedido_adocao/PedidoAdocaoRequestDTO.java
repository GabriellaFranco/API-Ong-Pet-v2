package com.enterprise.ong_pet2.dto.pedido_adocao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PedidoAdocaoRequestDTO(
        @NotNull(message = "ID do animal é obrigatório")
        Long idAnimal,

        @NotBlank(message = "Observações são obrigatórias")
        String observacoes
) {}
