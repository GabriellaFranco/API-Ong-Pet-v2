package com.enterprise.ong_pet2.model.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record UsuarioRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotBlank(message = "CEP é obrigatório")
        String cep,

        @NotBlank(message = "Cidade é obrigatória")
        String cidade,

        @NotBlank(message = "Bairro é obrigatório")
        String bairro,

        @NotBlank(message = "Rua é obrigatória")
        String rua,

        @NotNull(message = "Número do endereço é obrigatório")
        Long numEndereco,

        String telefone
) {}
