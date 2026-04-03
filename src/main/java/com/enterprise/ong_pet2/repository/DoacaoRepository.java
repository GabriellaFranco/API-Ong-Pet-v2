package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.Doacao;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.TipoDoacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    boolean existsByDoadorAndDataBetween(Usuario doador, LocalDateTime inicio, LocalDateTime fim);

    @Query("""
            SELECT d FROM Doacao d
            WHERE (:doador IS NULL OR LOWER(d.doador.nome) LIKE LOWER(CONCAT('%', :doador, '%')))
            AND (:categoria IS NULL OR d.categoria = :categoria)
            AND (:dataInicio IS NULL OR d.data >= :dataInicio)
            AND (:dataFim IS NULL OR d.data <= :dataFim)
            """)
    Page<Doacao> findByFilter(
            @Param("doador") String doador,
            @Param("categoria") TipoDoacao categoria,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}