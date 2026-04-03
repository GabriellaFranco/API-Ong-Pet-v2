package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.ItemEstoque;
import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemEstoqueRepository extends JpaRepository<ItemEstoque, Long> {

    @Query("""
            SELECT i FROM ItemEstoque i
            WHERE (:categoria IS NULL OR i.categoria = :categoria)
            AND (:ativo IS NULL OR i.ativo = :ativo)
            AND (:alertaEstoqueBaixo IS NULL OR :alertaEstoqueBaixo = false
                 OR i.quantidadeAtual <= i.quantidadeMinima)
            """)
    Page<ItemEstoque> findByFilter(
            @Param("categoria") CategoriaEstoque categoria,
            @Param("ativo") Boolean ativo,
            @Param("alertaEstoqueBaixo") Boolean alertaEstoqueBaixo,
            Pageable pageable
    );

    @Query("""
            SELECT i FROM ItemEstoque i
            WHERE i.ativo = true
            AND i.quantidadeAtual <= i.quantidadeMinima
            """)
    Page<ItemEstoque> findItensComEstoqueBaixo(Pageable pageable);
}