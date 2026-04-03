package com.enterprise.ong_pet2.repository;

import com.enterprise.ong_pet2.entity.Animal;
import com.enterprise.ong_pet2.entity.AnimalMidia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalMidiaRepository extends JpaRepository<AnimalMidia, Long> {

    List<AnimalMidia> findByAnimalOrderByOrdemAsc(Animal animal);

    Optional<AnimalMidia> findByAnimalAndPrincipalTrue(Animal animal);

    @Modifying
    @Query("UPDATE AnimalMidia m SET m.principal = false WHERE m.animal = :animal")
    void clearPrincipalByAnimal(@Param("animal") Animal animal);
}