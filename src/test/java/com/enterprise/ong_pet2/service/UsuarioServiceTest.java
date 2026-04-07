package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.TipoAutoridade;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.UsuarioMapper;
import com.enterprise.ong_pet2.model.dto.usuario.UsuarioRequestDTO;
import com.enterprise.ong_pet2.repository.AutoridadeRepository;
import com.enterprise.ong_pet2.repository.UsuarioRepository;
import com.enterprise.ong_pet2.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AutoridadeRepository autoridadeRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        var dto = new UsuarioRequestDTO(
                "João", "joao@email.com", "123.456.789-00",
                "senha123", "89000-000", "Blumenau",
                "Centro", "Rua XV", 100L, null
        );
        var autoridade = TestDataFactory.umaAutoridade(TipoAutoridade.ROLE_PADRAO);
        var usuario = TestDataFactory.umUsuarioPadrao();

        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(usuarioRepository.existsByCpf(dto.cpf())).thenReturn(false);
        when(autoridadeRepository.findByName(TipoAutoridade.ROLE_PADRAO.name()))
                .thenReturn(Optional.of(autoridade));
        when(usuarioMapper.toUsuario(dto)).thenReturn(usuario);
        when(passwordEncoder.encode(any())).thenReturn("senhaCriptografada");
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(usuarioMapper.toResponseDTO(usuario)).thenReturn(null);

        assertThatNoException().isThrownBy(() -> usuarioService.createUsuario(dto));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
    void deveLancarExcecaoEmailDuplicado() {
        var dto = new UsuarioRequestDTO(
                "João", "joao@email.com", "123.456.789-00",
                "senha123", "89000-000", "Blumenau",
                "Centro", "Rua XV", 100L, null
        );

        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.createUsuario(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com CPF duplicado")
    void deveLancarExcecaoCpfDuplicado() {
        var dto = new UsuarioRequestDTO(
                "João", "joao@email.com", "123.456.789-00",
                "senha123", "89000-000", "Blumenau",
                "Centro", "Rua XV", 100L, null
        );

        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(usuarioRepository.existsByCpf(dto.cpf())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.createUsuario(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF já cadastrado");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
    void deveLancarExcecaoUsuarioNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.getUsuarioById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}