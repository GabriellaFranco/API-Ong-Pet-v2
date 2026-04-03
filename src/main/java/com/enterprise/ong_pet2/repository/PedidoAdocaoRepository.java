package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.PedidoAdocao;
import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.StatusAdocao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PedidoAdocaoRepository extends JpaRepository<PedidoAdocao, Long> {

    boolean existsByAdotanteAndAnimalAndStatus(Usuario adotante, Animal animal, StatusAdocao status);

    long countByAdotanteAndStatus(Usuario adotante, StatusAdocao status);

    long countByVoluntarioResponsavelAndStatus(Usuario voluntario, StatusAdocao status);

    @Query("""
            SELECT p FROM PedidoAdocao p
            WHERE (:status IS NULL OR p.status = :status)
            AND (:dataPedido IS NULL OR p.dataPedido = :dataPedido)
            AND (:adotante IS NULL OR LOWER(p.adotante.nome) LIKE LOWER(CONCAT('%', :adotante, '%')))
            AND (:voluntario IS NULL OR LOWER(p.voluntarioResponsavel.nome) LIKE LOWER(CONCAT('%', :voluntario, '%')))
            """)
    Page<PedidoAdocao> findByFilter(
            @Param("status") StatusAdocao status,
            @Param("dataPedido") LocalDate dataPedido,
            @Param("adotante") String adotante,
            @Param("voluntario") String voluntario,
            Pageable pageable
    );
}