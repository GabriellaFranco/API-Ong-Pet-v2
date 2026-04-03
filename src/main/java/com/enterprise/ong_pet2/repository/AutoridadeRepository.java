package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.Autoridade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutoridadeRepository extends JpaRepository<Autoridade, Long> {

    Optional<Autoridade> findByName(String name);

    List<Autoridade> findByNameIn(List<String> names);
}