package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.Especie;
import com.enterprise.ong_pet2.enums.Genero;
import com.enterprise.ong_pet2.enums.PorteAnimal;
import com.enterprise.ong_pet2.model.dto.animal.AnimalRequestDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalResponseDTO;
import com.enterprise.ong_pet2.model.dto.animal.AnimalUpdateDTO;
import com.enterprise.ong_pet2.service.AnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Animais", description = "Cadastro e gestão de animais disponíveis para adoção")
public class AnimalController {

    private final AnimalService animalService;

    @Operation(
            summary = "Listar todos os animais",
            description = "Retorna todos os animais paginados. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum animal encontrado")
            }
    )
    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> getAllAnimais(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var animais = animalService.getAllAnimais(pageable);
        return animais.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animais);
    }

    @Operation(
            summary = "Buscar animais por filtro",
            description = "Filtra animais por nome, espécie, gênero, porte ou disponibilidade. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado retornado com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado")
            }
    )
    @GetMapping("/results")
    public ResponseEntity<Page<AnimalResponseDTO>> getAnimalsByFilter(
            @Parameter(description = "Parte do nome do animal")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Espécie: CANINO, FELINO ou OUTROS")
            @RequestParam(required = false) Especie especie,
            @Parameter(description = "Gênero: MASCULINO ou FEMININO")
            @RequestParam(required = false) Genero genero,
            @Parameter(description = "Porte: PEQUENO, MEDIO ou GRANDE")
            @RequestParam(required = false) PorteAnimal porte,
            @Parameter(description = "Disponível para adoção: true ou false")
            @RequestParam(required = false) Boolean disponivel,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var animais = animalService.getAnimalsByFilter(nome, especie, genero, porte, disponivel, pageable);
        return animais.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(animais);
    }

    @Operation(
            summary = "Buscar animal por ID",
            description = "Retorna um animal pelo ID. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Animal encontrado"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> getAnimalById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.getAnimalById(id));
    }

    @Operation(
            summary = "Cadastrar animal",
            description = "Cadastra um novo animal vinculado ao usuário autenticado. Requer autenticação.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Animal cadastrado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "422", description = "Animal já cadastrado com este nome e espécie")
            }
    )
    @PostMapping
    public ResponseEntity<AnimalResponseDTO> createAnimal(
            @Valid @RequestBody AnimalRequestDTO dto) {
        var animal = animalService.createAnimal(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(animal.id()).toUri();
        return ResponseEntity.created(uri).body(animal);
    }

    @Operation(
            summary = "Atualizar animal",
            description = "Atualiza disponibilidade, saúde e descrição do animal. Requer role VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Animal atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Animal já foi adotado")
            }
    )
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public ResponseEntity<AnimalResponseDTO> updateAnimal(
            @PathVariable Long id,
            @Valid @RequestBody AnimalUpdateDTO dto) {
        return ResponseEntity.ok(animalService.updateAnimal(id, dto));
    }

    @Operation(
            summary = "Excluir animal",
            description = "Remove um animal pelo ID. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Animal excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Animal já foi adotado")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return ResponseEntity.ok("Animal excluído com sucesso: " + id);
    }
}