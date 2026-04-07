package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.midia.AnimalMidiaResponseDTO;
import com.enterprise.ong_pet2.model.dto.midia.ReordenarMidiaDTO;
import com.enterprise.ong_pet2.service.MidiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Mídias dos Animais", description = "Upload e gestão de fotos e vídeos dos animais")
public class AnimalMidiaController {

    private final MidiaService mediaService;

    @Operation(
            summary = "Upload de mídia",
            description = "Faz upload de foto (JPEG, PNG, WebP — máx. 10MB) ou vídeo (MP4 — máx. 50MB). " +
                    "O tipo é detectado automaticamente pelo conteúdo do arquivo. " +
                    "A primeira mídia enviada é definida automaticamente como principal.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Tipo ou tamanho inválido"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO', 'PADRAO')")
    public ResponseEntity<AnimalMidiaResponseDTO> upload(
            @PathVariable Long animalId,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.ok(mediaService.upload(animalId, arquivo));
    }

    @Operation(
            summary = "Listar mídias do animal",
            description = "Retorna todas as mídias do animal ordenadas pela galeria. Endpoint público.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhuma mídia cadastrada"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado")
            }
    )
    @GetMapping
    public ResponseEntity<List<AnimalMidiaResponseDTO>> listar(@PathVariable Long animalId) {
        var midias = mediaService.listarMidias(animalId);
        return midias.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(midias);
    }

    @Operation(
            summary = "Definir foto principal",
            description = "Define a foto de capa do animal. Apenas fotos podem ser definidas como principal. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Foto principal definida com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Mídia não encontrada"),
                    @ApiResponse(responseCode = "422", description = "Mídia não pertence ao animal ou é um vídeo")
            }
    )
    @PatchMapping("/{midiaId}/principal")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<AnimalMidiaResponseDTO> definirPrincipal(
            @PathVariable Long animalId,
            @PathVariable Long midiaId) {
        return ResponseEntity.ok(mediaService.definirPrincipal(animalId, midiaId));
    }

    @Operation(
            summary = "Reordenar galeria",
            description = "Reordena as mídias da galeria. Envie uma lista de { midiaId, novaOrdem }. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Galeria reordenada com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Mídia não encontrada"),
                    @ApiResponse(responseCode = "422", description = "Mídia não pertence ao animal")
            }
    )
    @PatchMapping("/reordenar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<List<AnimalMidiaResponseDTO>> reordenar(
            @PathVariable Long animalId,
            @Valid @RequestBody List<ReordenarMidiaDTO> ordens) {
        return ResponseEntity.ok(mediaService.reordenar(animalId, ordens));
    }

    @Operation(
            summary = "Excluir mídia",
            description = "Remove uma mídia do animal e do storage. " +
                    "Se a mídia excluída era a principal, a próxima foto disponível é promovida automaticamente. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mídia excluída com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Mídia não encontrada"),
                    @ApiResponse(responseCode = "422", description = "Mídia não pertence ao animal")
            }
    )
    @DeleteMapping("/{midiaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<String> deletar(
            @PathVariable Long animalId,
            @PathVariable Long midiaId) {
        mediaService.deletarMidia(animalId, midiaId);
        return ResponseEntity.ok("Mídia excluída com sucesso");
    }
}