package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteRequestDTO;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteResponseDTO;
import com.enterprise.ong_pet2.service.PerfilAdotanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios/me/perfil-adotante")
public class PerfilAdotanteController {

    private final PerfilAdotanteService perfilAdotanteService;

    @GetMapping
    public ResponseEntity<PerfilAdotanteResponseDTO> getPerfil() {
        return ResponseEntity.ok(perfilAdotanteService.getPerfilDoUsuarioLogado());
    }

    @PostMapping
    public ResponseEntity<PerfilAdotanteResponseDTO> salvarPerfil(
            @Valid @RequestBody PerfilAdotanteRequestDTO dto) {
        return ResponseEntity.ok(perfilAdotanteService.salvarPerfil(dto));
    }
}