package com.enterprise.ong_pet2.dto.pedido_adocao;

import com.enterprise.ong_pet2.enums.StatusAdocao;

public record PedidoAdocaoUpdateDTO(
        StatusAdocao statusAdocao,
        String observacoes
) {}
