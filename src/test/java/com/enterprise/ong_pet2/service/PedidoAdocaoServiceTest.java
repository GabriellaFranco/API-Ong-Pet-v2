package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.enums.StatusAdocao;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.mapper.PedidoAdocaoMapper;
import com.enterprise.ong_pet2.messaging.publisher.EventPublisher;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoUpdateDTO;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.PedidoAdocaoRepository;
import com.enterprise.ong_pet2.repository.UsuarioRepository;
import com.enterprise.ong_pet2.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoAdocaoServiceTest {

    @InjectMocks
    private PedidoAdocaoService pedidoAdocaoService;

    @Mock private PedidoAdocaoRepository pedidoAdocaoRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PedidoAdocaoMapper pedidoAdocaoMapper;
    @Mock private UsuarioService usuarioService;
    @Mock private EventPublisher eventPublisher;

    @Test
    @DisplayName("Deve criar pedido de adoção com sucesso")
    void deveCriarPedidoComSucesso() {
        var adotante = TestDataFactory.umUsuarioPadrao();
        var voluntario = TestDataFactory.umVoluntario();
        var animal = TestDataFactory.umAnimal(adotante);
        var pedido = TestDataFactory.umPedidoAdocao(animal, adotante, voluntario);
        var dto = new PedidoAdocaoRequestDTO(1L, "Tenho experiência com cães");

        when(usuarioService.getUsuarioLogado()).thenReturn(adotante);
        when(pedidoAdocaoRepository.countByAdotanteAndStatus(adotante, StatusAdocao.SOLICITADA))
                .thenReturn(0L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(pedidoAdocaoRepository.existsByAdotanteAndAnimalAndStatus(any(), any(), any()))
                .thenReturn(false);
        when(usuarioRepository.findByPerfil(any())).thenReturn(List.of(voluntario));

        when(pedidoAdocaoMapper.toPedidoAdocao(any(), any(), any(), any())).thenReturn(pedido);
        when(pedidoAdocaoRepository.save(any())).thenReturn(pedido);

        assertThatNoException().isThrownBy(() -> pedidoAdocaoService.createPedido(dto));
        verify(pedidoAdocaoRepository).save(any());
        verify(eventPublisher).publish(any(), eq("adocao.criada"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando adotante possui 3 pedidos pendentes")
    void deveLancarExcecaoLimitePedidos() {
        var adotante = TestDataFactory.umUsuarioPadrao();
        var dto = new PedidoAdocaoRequestDTO(1L, "Observação");

        when(usuarioService.getUsuarioLogado()).thenReturn(adotante);
        when(pedidoAdocaoRepository.countByAdotanteAndStatus(adotante, StatusAdocao.SOLICITADA))
                .thenReturn(3L);

        assertThatThrownBy(() -> pedidoAdocaoService.createPedido(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("3 pedidos pendentes");

        verify(pedidoAdocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando animal não está disponível")
    void deveLancarExcecaoAnimalIndisponivel() {
        var adotante = TestDataFactory.umUsuarioPadrao();
        var animal = TestDataFactory.umAnimalIndisponivel(adotante);
        var dto = new PedidoAdocaoRequestDTO(1L, "Observação");

        when(usuarioService.getUsuarioLogado()).thenReturn(adotante);
        when(pedidoAdocaoRepository.countByAdotanteAndStatus(adotante, StatusAdocao.SOLICITADA))
                .thenReturn(0L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> pedidoAdocaoService.createPedido(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não disponível");

        verify(pedidoAdocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido duplicado para o mesmo animal")
    void deveLancarExcecaoPedidoDuplicado() {
        var adotante = TestDataFactory.umUsuarioPadrao();
        var animal = TestDataFactory.umAnimal(adotante);
        var dto = new PedidoAdocaoRequestDTO(1L, "Observação");

        when(usuarioService.getUsuarioLogado()).thenReturn(adotante);
        when(pedidoAdocaoRepository.countByAdotanteAndStatus(adotante, StatusAdocao.SOLICITADA))
                .thenReturn(0L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(pedidoAdocaoRepository.existsByAdotanteAndAnimalAndStatus(any(), any(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> pedidoAdocaoService.createPedido(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pedido pendente");

        verify(pedidoAdocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar pedido já avaliado")
    void deveLancarExcecaoAoDeletarPedidoAvaliado() {
        var adotante = TestDataFactory.umUsuarioPadrao();
        var voluntario = TestDataFactory.umVoluntario();
        var animal = TestDataFactory.umAnimal(adotante);
        var pedido = TestDataFactory.umPedidoAdocao(animal, adotante, voluntario);
        pedido.setStatus(StatusAdocao.APROVADA);

        when(pedidoAdocaoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoAdocaoService.deletePedido(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já avaliado");
    }

    @Test
    @DisplayName("Deve lançar exceção para transição de status inválida")
    void deveLancarExcecaoTransicaoStatusInvalida() {
        var adotante = TestDataFactory.umUsuarioPadrao();
        var voluntario = TestDataFactory.umVoluntario();
        var animal = TestDataFactory.umAnimal(adotante);
        var pedido = TestDataFactory.umPedidoAdocao(animal, adotante, voluntario);
        pedido.setStatus(StatusAdocao.REPROVADA);

        var dto = new PedidoAdocaoUpdateDTO(StatusAdocao.APROVADA, null);

        when(pedidoAdocaoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoAdocaoService.updateStatusPedido(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transição de status inválida");
    }
}