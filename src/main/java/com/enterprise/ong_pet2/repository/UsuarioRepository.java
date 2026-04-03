package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.Usuario;
import com.enterprise.ong_pet2.enums.PerfilUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByPerfil(PerfilUsuario perfil);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (:nome IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            AND (:cpf IS NULL OR u.cpf LIKE CONCAT('%', :cpf, '%'))
            AND (:cidade IS NULL OR LOWER(u.cidade) LIKE LOWER(CONCAT('%', :cidade, '%')))
            AND (:perfil IS NULL OR u.perfil = :perfil)
            """)
    Page<Usuario> findByFilter(
            @Param("nome") String nome,
            @Param("cpf") String cpf,
            @Param("cidade") String cidade,
            @Param("perfil") PerfilUsuario perfil,
            Pageable pageable
    );
}