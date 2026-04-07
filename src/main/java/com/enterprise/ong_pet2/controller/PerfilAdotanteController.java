package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteRequestDTO;
import com.enterprise.ong_pet2.model.dto.perfil_adotante.PerfilAdotanteResponseDTO;
import com.enterprise.ong_pet2.service.PerfilAdotanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios/me/perfil-adotante")
@Tag(name = "Perfil do Adotante", description = "Formulário de triagem e score de risco do adotante")
public class PerfilAdotanteController {

    private final PerfilAdotanteService perfilAdotanteService;

    @Operation(
            summary = "Consultar perfil",
            description = "Retorna o perfil de triagem do usuário autenticado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
                    @ApiResponse(responseCode = "404", description = "Perfil ainda não preenchido")
            }
    )
    @GetMapping
    public ResponseEntity<PerfilAdotanteResponseDTO> getPerfil() {
        return ResponseEntity.ok(perfilAdotanteService.getPerfilDoUsuarioLogado());
    }

    @Operation(
            summary = "Salvar perfil",
            description = "Cria ou atualiza o perfil de triagem do usuário autenticado. " +
                    "O score de risco (0-100) é calculado automaticamente — quanto maior, menor o risco.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Perfil salvo com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos")
            }
    )
    @PostMapping
    public ResponseEntity<PerfilAdotanteResponseDTO> salvarPerfil(
            @Valid @RequestBody PerfilAdotanteRequestDTO dto) {
        return ResponseEntity.ok(perfilAdotanteService.salvarPerfil(dto));
    }
}