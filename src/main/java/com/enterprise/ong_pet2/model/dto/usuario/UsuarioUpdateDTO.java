package com.enterprise.ong_pet2.model.dto.usuario;

import jakarta.validation.constraints.Email;

public record UsuarioUpdateDTO(
        @Email(message = "Email inválido")
        String email,
        String cep,
        String cidade,
        String bairro,
        String rua,
        Long numEndereco,
        String telefone
) {}
