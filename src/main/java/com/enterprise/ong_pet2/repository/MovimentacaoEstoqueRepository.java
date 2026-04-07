package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.ItemEstoque;
import com.enterprise.ong_pet2.entity.MovimentacaoEstoque;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    Page<MovimentacaoEstoque> findByItemOrderByDataMovimentacaoDesc(ItemEstoque item, Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(CASE WHEN m.tipo = 'ENTRADA' THEN m.quantidade ELSE -m.quantidade END), 0)
            FROM MovimentacaoEstoque m
            WHERE m.item = :item
            """)
    BigDecimal calcularSaldoAtual(@Param("item") ItemEstoque item);

    @Query("""
            SELECT m FROM MovimentacaoEstoque m
            WHERE m.item = :item
            AND (:tipo IS NULL OR m.tipo = :tipo)
            AND (:dataInicio IS NULL OR m.dataMovimentacao >= :dataInicio)
            AND (:dataFim IS NULL OR m.dataMovimentacao <= :dataFim)
            ORDER BY m.dataMovimentacao DESC
            """)
    Page<MovimentacaoEstoque> findByFilter(
            @Param("item") ItemEstoque item,
            @Param("tipo") TipoMovimentacao tipo,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}