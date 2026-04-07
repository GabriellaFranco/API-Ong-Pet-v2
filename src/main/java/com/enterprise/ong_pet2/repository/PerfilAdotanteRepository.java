package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.PerfilAdotante;
import com.enterprise.ong_pet2.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilAdotanteRepository extends JpaRepository<PerfilAdotante, Long> {

    Optional<PerfilAdotante> findByUsuario(Usuario usuario);

    boolean existsByUsuario(Usuario usuario);
}