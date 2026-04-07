package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaRequestDTO;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaResponseDTO;
import com.enterprise.ong_pet2.service.DespesaService;
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

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/despesas")
@Tag(name = "Despesas", description = "Registro e consulta de despesas da ONG")
public class DespesaController {

    private final DespesaService despesaService;

    @Operation(
            summary = "Buscar despesas por filtro",
            description = "Filtra despesas por categoria, período e animal. Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado retornado com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhuma despesa encontrada")
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<DespesaResponseDTO>> getDespesasByFilter(
            @Parameter(description = "Categoria: RACAO, MEDICAMENTO, ACESSORIO, HIGIENE ou OUTRO")
            @RequestParam(required = false) CategoriaEstoque categoria,
            @Parameter(description = "Data início (formato: yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate dataInicio,
            @Parameter(description = "Data fim (formato: yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate dataFim,
            @Parameter(description = "ID do animal vinculado")
            @RequestParam(required = false) Long idAnimal,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var despesas = despesaService.getDespesasByFilter(
                categoria, dataInicio, dataFim, idAnimal, pageable);
        return despesas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(despesas);
    }

    @Operation(
            summary = "Buscar despesa por ID",
            description = "Retorna uma despesa pelo ID. Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Despesa encontrada"),
                    @ApiResponse(responseCode = "404", description = "Despesa não encontrada")
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<DespesaResponseDTO> getDespesaById(@PathVariable Long id) {
        return ResponseEntity.ok(despesaService.getDespesaById(id));
    }

    @Operation(
            summary = "Registrar despesa",
            description = "Registra uma nova despesa. Pode ser vinculada a um animal específico. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Despesa registrada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Animal não encontrado")
            }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<DespesaResponseDTO> createDespesa(
            @Valid @RequestBody DespesaRequestDTO dto) {
        var despesa = despesaService.createDespesa(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(despesa.id()).toUri();
        return ResponseEntity.created(uri).body(despesa);
    }

    @Operation(
            summary = "Excluir despesa",
            description = "Remove uma despesa pelo ID. Requer role ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Despesa excluída com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Despesa não encontrada")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDespesa(@PathVariable Long id) {
        despesaService.deleteDespesa(id);
        return ResponseEntity.ok("Despesa excluída com sucesso: " + id);
    }
}