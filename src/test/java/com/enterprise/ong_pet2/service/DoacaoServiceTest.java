package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.mapper.DoacaoMapper;
import com.enterprise.ong_pet2.messaging.publisher.EventPublisher;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoRequestDTO;
import com.enterprise.ong_pet2.enums.TipoDoacao;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.DoacaoRepository;
import com.enterprise.ong_pet2.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {

    @InjectMocks
    private DoacaoService doacaoService;

    @Mock private DoacaoRepository doacaoRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private DoacaoMapper doacaoMapper;
    @Mock private UsuarioService usuarioService;
    @Mock private EventPublisher eventPublisher;

    @Test
    @DisplayName("Deve criar doação com sucesso")
    void deveCriarDoacaoComSucesso() {
        var doador = TestDataFactory.umUsuarioPadrao();
        var doacao = TestDataFactory.umaDoacao(doador);
        var dto = new DoacaoRequestDTO(
                new BigDecimal("50.00"), TipoDoacao.DINHEIRO, "Doação mensal", null
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(doador);
        when(doacaoRepository.existsByDoadorAndDataBetween(any(), any(), any()))
                .thenReturn(false);
        when(doacaoMapper.toDoacao(any(), any(), nullable(Animal.class)))
                .thenReturn(doacao);
        when(doacaoRepository.save(any())).thenReturn(doacao);

        assertThatNoException().isThrownBy(() -> doacaoService.createDoacao(dto));
        verify(doacaoRepository).save(any());
        verify(eventPublisher).publish(any(), eq("doacao.criada"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor da doação é menor que R$1,00")
    void deveLancarExcecaoValorMinimo() {
        var doador = TestDataFactory.umUsuarioPadrao();
        var dto = new DoacaoRequestDTO(
                new BigDecimal("0.50"), TipoDoacao.DINHEIRO, null, null
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(doador);

        assertThatThrownBy(() -> doacaoService.createDoacao(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("R$1,00");

        verify(doacaoRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any(), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando doação recente já registrada")
    void deveLancarExcecaoIntervaloMinimo() {
        var doador = TestDataFactory.umUsuarioPadrao();
        var dto = new DoacaoRequestDTO(
                new BigDecimal("50.00"), TipoDoacao.DINHEIRO, null, null
        );

        when(usuarioService.getUsuarioLogado()).thenReturn(doador);
        when(doacaoRepository.existsByDoadorAndDataBetween(any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> doacaoService.createDoacao(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("1 minuto");

        verify(doacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar doação de outro usuário")
    void deveLancarExcecaoAoDeletarDoacaoDeOutroUsuario() {
        var doador = TestDataFactory.umUsuarioPadrao();
        var outroUsuario = TestDataFactory.umVoluntario();
        var doacao = TestDataFactory.umaDoacao(doador);

        when(doacaoRepository.findById(1L)).thenReturn(Optional.of(doacao));
        when(usuarioService.getUsuarioLogado()).thenReturn(outroUsuario);

        assertThatThrownBy(() -> doacaoService.deleteDoacao(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("permissão");
    }
}