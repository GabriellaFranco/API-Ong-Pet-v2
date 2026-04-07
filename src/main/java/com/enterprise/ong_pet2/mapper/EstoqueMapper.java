package com.enterprise.ong_pet2.mapper;

import com.enterprise.ong_pet2.entity.ItemEstoque;
import com.enterprise.ong_pet2.entity.MovimentacaoEstoque;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueResponseDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class EstoqueMapper {

    public ItemEstoque toItemEstoque(ItemEstoqueRequestDTO dto) {
        return ItemEstoque.builder()
                .nome(dto.nome())
                .categoria(dto.categoria())
                .unidadeMedida(dto.unidadeMedida())
                .quantidadeMinima(dto.quantidadeMinima() != null
                        ? dto.quantidadeMinima()
                        : java.math.BigDecimal.ZERO)
                .descricao(dto.descricao())
                .build();
    }

    public ItemEstoqueResponseDTO toItemResponseDTO(ItemEstoque item) {
        boolean estoqueBaixo = item.getQuantidadeAtual()
                .compareTo(item.getQuantidadeMinima()) <= 0;

        return new ItemEstoqueResponseDTO(
                item.getId(),
                item.getNome(),
                item.getCategoria(),
                item.getUnidadeMedida(),
                item.getQuantidadeAtual(),
                item.getQuantidadeMinima(),
                item.getDescricao(),
                item.getAtivo(),
                estoqueBaixo
        );
    }

    public MovimentacaoResponseDTO toMovimentacaoResponseDTO(MovimentacaoEstoque mov) {
        return new MovimentacaoResponseDTO(
                mov.getId(),
                mov.getTipo(),
                mov.getQuantidade(),
                mov.getDataMovimentacao(),
                mov.getMotivo(),
                mov.getObservacoes(),
                mov.getResponsavel().getNome()
        );
    }
}
