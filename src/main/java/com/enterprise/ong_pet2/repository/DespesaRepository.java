package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.Despesa;
import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DespesaRepository extends JpaRepository<Despesa, Long> {

    @Query("""
            SELECT d FROM Despesa d
            WHERE (:categoria IS NULL OR d.categoria = :categoria)
            AND (:dataInicio IS NULL OR d.data >= :dataInicio)
            AND (:dataFim IS NULL OR d.data <= :dataFim)
            AND (:idAnimal IS NULL OR d.animal.id = :idAnimal)
            """)
    Page<Despesa> findByFilter(
            @Param("categoria") CategoriaEstoque categoria,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("idAnimal") Long idAnimal,
            Pageable pageable
    );
}