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

    @Query(value = """
        SELECT d.* FROM doacoes d
        JOIN usuarios u ON u.id = d.id_doador
        WHERE (CAST(:doador AS varchar) IS NULL OR LOWER(u.nome::varchar) ILIKE LOWER(CONCAT('%', CAST(:doador AS varchar), '%')))
        AND (CAST(:categoria AS varchar) IS NULL OR d.categoria = CAST(:categoria AS varchar))
        AND (CAST(:dataInicio AS timestamp) IS NULL OR d.data >= CAST(:dataInicio AS timestamp))
        AND (CAST(:dataFim AS timestamp) IS NULL OR d.data <= CAST(:dataFim AS timestamp))
        ORDER BY d.id DESC
        """,
            countQuery = """
        SELECT COUNT(*) FROM doacoes d
        JOIN usuarios u ON u.id = d.id_doador
        WHERE (CAST(:doador AS varchar) IS NULL OR LOWER(u.nome::varchar) ILIKE LOWER(CONCAT('%', CAST(:doador AS varchar), '%')))
        AND (CAST(:categoria AS varchar) IS NULL OR d.categoria = CAST(:categoria AS varchar))
        AND (CAST(:dataInicio AS timestamp) IS NULL OR d.data >= CAST(:dataInicio AS timestamp))
        AND (CAST(:dataFim AS timestamp) IS NULL OR d.data <= CAST(:dataFim AS timestamp))
        """,
            nativeQuery = true)
    Page<Doacao> findByFilter(
            @Param("doador") String doador,
            @Param("categoria") String categoria,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}