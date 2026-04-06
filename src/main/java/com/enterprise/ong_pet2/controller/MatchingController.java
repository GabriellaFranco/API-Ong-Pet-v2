package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.matching.MatchingResponseDTO;
import com.enterprise.ong_pet2.service.MatchingService;
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
public class MatchingController {

    private final MatchingService matchingService;

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

    @GetMapping("/usuarios/me/animais-sugeridos")
    public ResponseEntity<Page<MatchingResponseDTO>> getAnimaisSugeridos(
            @PageableDefault(size = 10) Pageable pageable) {
        var resultado = matchingService.getAnimaisSugeridosParaAdotante(pageable);
        return resultado.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(resultado);
    }
}