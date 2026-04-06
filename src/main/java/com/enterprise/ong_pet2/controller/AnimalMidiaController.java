package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.midia.AnimalMidiaResponseDTO;
import com.enterprise.ong_pet2.model.dto.midia.ReordenarMidiaDTO;
import com.enterprise.ong_pet2.service.MidiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/animais/{animalId}/midias")
public class AnimalMidiaController {

    private final MidiaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnimalMidiaResponseDTO> upload(
            @PathVariable Long animalId,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(mediaService.upload(animalId, arquivo));
    }

    @GetMapping
    public ResponseEntity<List<AnimalMidiaResponseDTO>> listar(@PathVariable Long animalId) {
        var midias = mediaService.listarMidias(animalId);
        return midias.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(midias);
    }

    @PatchMapping("/{midiaId}/principal")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO', 'PADRAO')")
    public ResponseEntity<AnimalMidiaResponseDTO> definirPrincipal(
            @PathVariable Long animalId,
            @PathVariable Long midiaId) {
        return ResponseEntity.ok(mediaService.definirPrincipal(animalId, midiaId));
    }

    @PatchMapping("/reordenar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO', 'PADRAO')")
    public ResponseEntity<List<AnimalMidiaResponseDTO>> reordenar(
            @PathVariable Long animalId,
            @Valid @RequestBody List<ReordenarMidiaDTO> ordens) {
        return ResponseEntity.ok(mediaService.reordenar(animalId, ordens));
    }

    @DeleteMapping("/{midiaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO', 'PADRAO')")
    public ResponseEntity<String> deletar(
            @PathVariable Long animalId,
            @PathVariable Long midiaId) {
        mediaService.deletarMidia(animalId, midiaId);
        return ResponseEntity.ok("Mídia excluída com sucesso");
    }
}