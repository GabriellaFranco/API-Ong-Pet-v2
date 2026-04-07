package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.PerfilAdotanteMapper;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteRequestDTO;
import com.enterprise.ong_pet2.enums.*;
import com.enterprise.ong_pet2.repository.PerfilAdotanteRepository;
import com.enterprise.ong_pet2.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilAdotanteServiceTest {

    @InjectMocks
    private PerfilAdotanteService perfilAdotanteService;

    @Mock private PerfilAdotanteRepository perfilAdotanteRepository;
    @Mock private PerfilAdotanteMapper perfilAdotanteMapper;
    @Mock private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve criar perfil de adotante com sucesso")
    void deveCriarPerfilComSucesso() {
        var usuario = TestDataFactory.umUsuarioPadrao();
        var perfil = TestDataFactory.umPerfilAdotante(usuario);
        var dto = new PerfilAdotanteRequestDTO(
                TipoMoradia.CASA_COM_QUINTAL, 80, false, null,
                "Trabalho em casa", 10, FaixaRenda.DE_2_A_5SM,
                true, "Quero dar um lar", Especie.CANINA,
                PorteAnimal.MEDIO, true
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(usuario);
        when(perfilAdotanteRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
        when(perfilAdotanteMapper.toPerfilAdotante(dto, usuario)).thenReturn(perfil);
        when(perfilAdotanteRepository.save(any())).thenReturn(perfil);
        when(perfilAdotanteMapper.toResponseDTO(any())).thenReturn(null);

        assertThatNoException().isThrownBy(() -> perfilAdotanteService.salvarPerfil(dto));
        verify(perfilAdotanteRepository).save(any());
    }

    @Test
    @DisplayName("Deve atualizar perfil existente ao invés de criar novo")
    void deveAtualizarPerfilExistente() {
        var usuario = TestDataFactory.umUsuarioPadrao();
        var perfil = TestDataFactory.umPerfilAdotante(usuario);
        var dto = new PerfilAdotanteRequestDTO(
                TipoMoradia.APARTAMENTO, 50, true, "Um gato",
                "Trabalho fora", 4, FaixaRenda.ATE_2SM,
                false, "Quero companhia", Especie.FELINA,
                PorteAnimal.PEQUENO, false
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(usuario);
        when(perfilAdotanteRepository.findByUsuario(usuario)).thenReturn(Optional.of(perfil));
        when(perfilAdotanteRepository.save(any())).thenReturn(perfil);
        when(perfilAdotanteMapper.toResponseDTO(any())).thenReturn(null);

        assertThatNoException().isThrownBy(() -> perfilAdotanteService.salvarPerfil(dto));

        // Verifica que updateFromDTO foi chamado (atualização) e não toPerfilAdotante (criação)
        verify(perfilAdotanteMapper).updateFromDTO(dto, perfil);
        verify(perfilAdotanteMapper, never()).toPerfilAdotante(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar perfil inexistente")
    void deveLancarExcecaoPerfilNaoEncontrado() {
        var usuario = TestDataFactory.umUsuarioPadrao();

        when(usuarioService.getUsuarioLogado()).thenReturn(usuario);
        when(perfilAdotanteRepository.findByUsuario(usuario)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilAdotanteService.getPerfilDoUsuarioLogado())
                .isInstanceOf(ResourceNotFoundException.class);
    }
}