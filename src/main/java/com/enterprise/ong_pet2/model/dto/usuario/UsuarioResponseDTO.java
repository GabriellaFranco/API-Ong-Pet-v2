package com.enterprise.ong_pet2.model.dto.usuario;

import com.enterprise.ong_pet2.enums.PerfilUsuario;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String cpf,
        String cep,
        String cidade,
        String bairro,
        String rua,
        Long numEndereco,
        String telefone,
        PerfilUsuario perfil
) {}
