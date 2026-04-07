package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.matching.MatchingResponseDTO;
import com.enterprise.ong_pet2.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Matching", description = "Recomendação inteligente de adoções com score explicável")
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(
            summary = "Adotantes compatíveis com um animal",
            description = "Retorna adotantes ordenados por score de compatibilidade com o animal informado. " +
                    "Score de 0 a 100 com fatores explicados. Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum adotante com perfil cadastrado"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Animal não está disponível para adoção")
            }
    )
    @GetMapping("/animais/{id}/matching")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<MatchingResponseDTO>> getAdotantesParaAnimal(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        var resultado = matchingService.getAdotantesParaAnimal(id, pageable);
        return resultado.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(resultado);
    }

    @Operation(
            summary = "Animais sugeridos para o adotante",
            description = "Retorna animais disponíveis ordenados por compatibilidade com o perfil do adotante logado. " +
                    "Requer perfil de adotante preenchido em /usuarios/me/perfil-adotante.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum animal disponível"),
                    @ApiResponse(responseCode = "404", description = "Perfil de adotante não encontrado")
            }
    )
    @GetMapping("/usuarios/me/animais-sugeridos")
    public ResponseEntity<Page<MatchingResponseDTO>> getAnimaisSugeridos(
            @PageableDefault(size = 10) Pageable pageable) {
        var resultado = matchingService.getAnimaisSugeridosParaAdotante(pageable);
        return resultado.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(resultado);
    }
}