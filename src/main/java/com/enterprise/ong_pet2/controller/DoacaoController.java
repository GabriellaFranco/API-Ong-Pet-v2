package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.TipoDoacao;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoResponseDTO;
import com.enterprise.ong_pet2.service.DoacaoService;
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

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doacoes")
@Tag(name = "Doações", description = "Registro e consulta de doações")
public class DoacaoController {

    private final DoacaoService doacaoService;

    @Operation(
            summary = "Listar todas as doações",
            description = "Retorna todas as doações paginadas. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhuma doação encontrada"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão")
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DoacaoResponseDTO>> getAllDoacoes(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var doacoes = doacaoService.getAllDoacoes(pageable);
        return doacoes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(doacoes);
    }

    @Operation(
            summary = "Buscar doações por filtro",
            description = "Filtra doações por doador, categoria e período. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado retornado com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado")
            }
    )
    @GetMapping("/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DoacaoResponseDTO>> getDoacoesByFilter(
            @Parameter(description = "Nome do doador")
            @RequestParam(required = false) String doador,
            @Parameter(description = "Categoria: DINHEIRO, RACAO, MEDICAMENTO, ACESSORIO ou OUTRO")
            @RequestParam(required = false) TipoDoacao categoria,
            @Parameter(description = "Data início (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) LocalDateTime dataInicio,
            @Parameter(description = "Data fim (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) LocalDateTime dataFim,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var doacoes = doacaoService.getDoacoesByFilter(
                doador, categoria, dataInicio, dataFim, pageable);
        return doacoes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(doacoes);
    }

    @Operation(
            summary = "Buscar doação por ID",
            description = "Retorna uma doação pelo ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Doação encontrada"),
                    @ApiResponse(responseCode = "404", description = "Doação não encontrada")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<DoacaoResponseDTO> getDoacaoById(@PathVariable Long id) {
        return ResponseEntity.ok(doacaoService.getDoacaoById(id));
    }

    @Operation(
            summary = "Registrar doação",
            description = "Registra uma nova doação. Valor mínimo: R$1,00. " +
                    "Intervalo mínimo entre doações: 1 minuto. " +
                    "Categorias físicas (RACAO, MEDICAMENTO) podem ser vinculadas ao estoque.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Doação registrada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "422", description = "Valor abaixo do mínimo ou intervalo não respeitado")
            }
    )
    @PostMapping
    public ResponseEntity<DoacaoResponseDTO> createDoacao(
            @Valid @RequestBody DoacaoRequestDTO dto) {
        var doacao = doacaoService.createDoacao(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(doacao.id()).toUri();
        return ResponseEntity.created(uri).body(doacao);
    }

    @Operation(
            summary = "Excluir doação",
            description = "Remove uma doação. ADMIN pode excluir qualquer doação. " +
                    "Usuário PADRAO só pode excluir as próprias doações.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Doação excluída com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Doação não encontrada")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRAO')")
    public ResponseEntity<String> deleteDoacao(@PathVariable Long id) {
        doacaoService.deleteDoacao(id);
        return ResponseEntity.ok("Doação excluída com sucesso: " + id);
    }
}