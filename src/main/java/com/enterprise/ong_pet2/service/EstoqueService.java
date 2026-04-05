package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.MovimentacaoEstoque;
import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.enums.MotivoMovimentacao;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;
import com.enterprise.ong_pet2.exception.BusinessException;
import com.enterprise.ong_pet2.exception.ResourceNotFoundException;
import com.enterprise.ong_pet2.mapper.EstoqueMapper;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueResponseDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoResponseDTO;
import com.enterprise.ong_pet2.repository.AnimalRepository;
import com.enterprise.ong_pet2.repository.DoacaoRepository;
import com.enterprise.ong_pet2.repository.ItemEstoqueRepository;
import com.enterprise.ong_pet2.repository.MovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ItemEstoqueRepository itemEstoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final AnimalRepository animalRepository;
    private final DoacaoRepository doacaoRepository;
    private final EstoqueMapper estoqueMapper;
    private final UsuarioService usuarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<ItemEstoqueResponseDTO> getItensByFilter(CategoriaEstoque categoria,
                                                         Boolean ativo,
                                                         Boolean alertaEstoqueBaixo,
                                                         Pageable pageable) {
        return itemEstoqueRepository.findByFilter(categoria, ativo, alertaEstoqueBaixo, pageable)
                .map(estoqueMapper::toItemResponseDTO);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<ItemEstoqueResponseDTO> getItensComEstoqueBaixo(Pageable pageable) {
        return itemEstoqueRepository.findItensComEstoqueBaixo(pageable)
                .map(estoqueMapper::toItemResponseDTO);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ItemEstoqueResponseDTO createItem(ItemEstoqueRequestDTO dto) {
        var item = estoqueMapper.toItemEstoque(dto);
        return estoqueMapper.toItemResponseDTO(itemEstoqueRepository.save(item));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public MovimentacaoResponseDTO registrarMovimentacao(Long idItem, MovimentacaoRequestDTO dto) {
        var item = itemEstoqueRepository.findById(idItem)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado: " + idItem));

        if (!item.getAtivo()) {
            throw new BusinessException("Não é possível movimentar um item inativo");
        }

        if (dto.motivo() == MotivoMovimentacao.AJUSTE_INVENTARIO) {
            var usuarioLogado = usuarioService.getUsuarioLogado();
            if (!usuarioLogado.getPerfil().name().equals("ADMIN")) {
                throw new BusinessException("Apenas ADMIN pode realizar ajuste de inventário");
            }
        }

        var saldoAtual = movimentacaoEstoqueRepository.calcularSaldoAtual(item);

        if (dto.tipo() == TipoMovimentacao.SAIDA) {
            if (saldoAtual.compareTo(dto.quantidade()) < 0) {
                throw new BusinessException("Saldo insuficiente. Saldo atual: " + saldoAtual);
            }
        }

        var responsavel = usuarioService.getUsuarioLogado();

        var movimentacao = MovimentacaoEstoque.builder()
                .item(item)
                .tipo(dto.tipo())
                .quantidade(dto.quantidade())
                .dataMovimentacao(LocalDateTime.now())
                .motivo(dto.motivo())
                .observacoes(dto.observacoes())
                .responsavel(responsavel)
                .build();

        if (dto.idAnimal() != null) {
            var animal = animalRepository.findById(dto.idAnimal())
                    .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + dto.idAnimal()));
            movimentacao.setAnimal(animal);
        }

        if (dto.idDoacao() != null) {
            var doacao = doacaoRepository.findById(dto.idDoacao())
                    .orElseThrow(() -> new ResourceNotFoundException("Doação não encontrada: " + dto.idDoacao()));
            movimentacao.setDoacao(doacao);
        }

        var salvo = movimentacaoEstoqueRepository.save(movimentacao);

        var novoSaldo = movimentacaoEstoqueRepository.calcularSaldoAtual(item);
        item.setQuantidadeAtual(novoSaldo);
        itemEstoqueRepository.save(item);

        return estoqueMapper.toMovimentacaoResponseDTO(salvo);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public Page<MovimentacaoResponseDTO> getMovimentacoesByItem(Long idItem,
                                                                TipoMovimentacao tipo,
                                                                LocalDateTime dataInicio,
                                                                LocalDateTime dataFim,
                                                                Pageable pageable) {
        var item = itemEstoqueRepository.findById(idItem)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado: " + idItem));

        return movimentacaoEstoqueRepository.findByFilter(item, tipo, dataInicio, dataFim, pageable)
                .map(estoqueMapper::toMovimentacaoResponseDTO);
    }
}