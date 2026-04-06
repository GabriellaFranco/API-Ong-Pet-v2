package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.Genero;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.model.dto.animal.AnimalRequestDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalResponseDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalUpdateDTO;
import com.enterprise.ong_pet2.service.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/animais")
public class AnimalController {

    private final AnimalService animalService;

    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> getAllAnimais(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var animais = animalService.getAllAnimais(pageable);
        return animais.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animais);
    }

    @GetMapping("/results")
    public ResponseEntity<Page<AnimalResponseDTO>> getAnimalsByFilter(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) Genero genero,
            @RequestParam(required = false) PorteAnimal porte,
            @RequestParam(required = false) Boolean disponivel,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var animais = animalService.getAnimalsByFilter(nome, especie, genero, porte, disponivel, pageable);
        return animais.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> getAnimalById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.getAnimalById(id));
    }

    @PostMapping
    public ResponseEntity<AnimalResponseDTO> createAnimal(@Valid @RequestBody AnimalRequestDTO dto) {
        var animal = animalService.createAnimal(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(animal.id()).toUri();
        return ResponseEntity.created(uri).body(animal);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public ResponseEntity<AnimalResponseDTO> updateAnimal(@PathVariable Long id,
                                                          @Valid @RequestBody AnimalUpdateDTO dto) {
        return ResponseEntity.ok(animalService.updateAnimal(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return ResponseEntity.ok("Animal excluído com sucesso: " + id);
    }
}