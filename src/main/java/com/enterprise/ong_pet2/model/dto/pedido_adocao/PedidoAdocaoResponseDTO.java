package com.enterprise.ong_pet2.model.dto.pedido_adocao;

import com.enterprise.ong_pet2.enums.PerfilUsuario;
import com.enterprise.ong_pet2.enums.StatusAdocao;

import java.time.LocalDate;

public record PedidoAdocaoResponseDTO(
        Long id,
        LocalDate dataPedido,
        StatusAdocao status,
        String observacoes,
        Integer scoreMatching,
        UsuarioDTO adotante,
        UsuarioDTO voluntarioResponsavel,
        AnimalDTO animal
) {
    public record UsuarioDTO(Long id, String nome, PerfilUsuario perfil) {}
    public record AnimalDTO(Long id, String nome) {}
}
