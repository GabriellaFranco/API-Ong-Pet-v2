package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.enums.MotivoMovimentacao;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.mapper.EstoqueMapper;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoRequestDTO;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.DoacaoRepository;
import com.enterprise.ong_pet2.repository.ItemEstoqueRepository;
import com.enterprise.ong_pet2.repository.MovimentacaoEstoqueRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @InjectMocks
    private EstoqueService estoqueService;

    @Mock private ItemEstoqueRepository itemEstoqueRepository;
    @Mock private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Mock private AnimalRepository animalRepository;
    @Mock private DoacaoRepository doacaoRepository;
    @Mock private EstoqueMapper estoqueMapper;
    @Mock private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve registrar entrada no estoque com sucesso")
    void deveRegistrarEntradaComSucesso() {
        var item = TestDataFactory.umItemEstoque();
        var responsavel = TestDataFactory.umVoluntario();
        var dto = new MovimentacaoRequestDTO(
                TipoMovimentacao.ENTRADA,
                new BigDecimal("5.0"),
                MotivoMovimentacao.DOACAO_RECEBIDA,
                null, null, null
        );

        when(itemEstoqueRepository.findById(1L)).thenReturn(Optional.of(item));
        when(usuarioService.getUsuarioLogado()).thenReturn(responsavel);
        when(movimentacaoEstoqueRepository.calcularSaldoAtual(item))
                .thenReturn(new BigDecimal("10.0"));
        when(movimentacaoEstoqueRepository.save(any())).thenReturn(null);
        when(movimentacaoEstoqueRepository.calcularSaldoAtual(item))
                .thenReturn(new BigDecimal("15.0"));
        when(itemEstoqueRepository.save(any())).thenReturn(item);
        when(estoqueMapper.toMovimentacaoResponseDTO(any())).thenReturn(null);

        assertThatNoException().isThrownBy(
                () -> estoqueService.registrarMovimentacao(1L, dto));
        verify(movimentacaoEstoqueRepository).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar saída com saldo insuficiente")
    void deveLancarExcecaoSaldoInsuficiente() {
        var item = TestDataFactory.umItemEstoque();
        item.setQuantidadeAtual(new BigDecimal("1.0"));
        var dto = new MovimentacaoRequestDTO(
                TipoMovimentacao.SAIDA,
                new BigDecimal("5.0"),
                MotivoMovimentacao.USO_ANIMAL,
                null, null, null
        );

        when(itemEstoqueRepository.findById(1L)).thenReturn(Optional.of(item));
        when(movimentacaoEstoqueRepository.calcularSaldoAtual(item))
                .thenReturn(new BigDecimal("1.0"));

        assertThatThrownBy(() -> estoqueService.registrarMovimentacao(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(movimentacaoEstoqueRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao movimentar item inativo")
    void deveLancarExcecaoItemInativo() {
        var item = TestDataFactory.umItemEstoque();
        item.setAtivo(false);
        var dto = new MovimentacaoRequestDTO(
                TipoMovimentacao.ENTRADA,
                new BigDecimal("5.0"),
                MotivoMovimentacao.DOACAO_RECEBIDA,
                null, null, null
        );

        when(itemEstoqueRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> estoqueService.registrarMovimentacao(1L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inativo");
    }
}