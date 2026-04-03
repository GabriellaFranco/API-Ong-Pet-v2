package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.dto.usuario.UsuarioRequestDTO;
import com.enterprise.ong_pet2.dto.usuario.UsuarioResponseDTO;
import com.enterprise.ong_pet2.dto.usuario.UsuarioUpdateDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioMapper {

    public Usuario toUsuario(UsuarioRequestDTO dto) {
        return Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .cpf(dto.cpf())
                .senha(dto.senha())
                .cep(dto.cep())
                .cidade(dto.cidade())
                .bairro(dto.bairro())
                .rua(dto.rua())
                .numEndereco(dto.numEndereco())
                .telefone(dto.telefone())
                .build();
    }

    public UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getCpf(),
                usuario.getCep(),
                usuario.getCidade(),
                usuario.getBairro(),
                usuario.getRua(),
                usuario.getNumEndereco(),
                usuario.getTelefone(),
                usuario.getPerfil()
        );
    }

    public void updateFromDTO(UsuarioUpdateDTO dto, Usuario usuario) {
        Optional.ofNullable(dto.email()).ifPresent(usuario::setEmail);
        Optional.ofNullable(dto.cep()).ifPresent(usuario::setCep);
        Optional.ofNullable(dto.cidade()).ifPresent(usuario::setCidade);
        Optional.ofNullable(dto.bairro()).ifPresent(usuario::setBairro);
        Optional.ofNullable(dto.rua()).ifPresent(usuario::setRua);
        Optional.ofNullable(dto.numEndereco()).ifPresent(usuario::setNumEndereco);
        Optional.ofNullable(dto.telefone()).ifPresent(usuario::setTelefone);
    }
}