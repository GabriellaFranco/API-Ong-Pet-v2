package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaRequestDTO;
import com.enterprise.ong_pet2.model.dto.despesa.DespesaResponseDTO;
import com.enterprise.ong_pet2.service.DespesaService;
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
public class DespesaController {

    private final DespesaService despesaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<DespesaResponseDTO>> getDespesasByFilter(
            @RequestParam(required = false) CategoriaEstoque categoria,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(required = false) Long idAnimal,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var despesas = despesaService.getDespesasByFilter(categoria, dataInicio, dataFim, idAnimal, pageable);
        return despesas.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(despesas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<DespesaResponseDTO> getDespesaById(@PathVariable Long id) {
        return ResponseEntity.ok(despesaService.getDespesaById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<DespesaResponseDTO> createDespesa(@Valid @RequestBody DespesaRequestDTO dto) {
        var despesa = despesaService.createDespesa(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(despesa.id()).toUri();
        return ResponseEntity.created(uri).body(despesa);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDespesa(@PathVariable Long id) {
        despesaService.deleteDespesa(id);
        return ResponseEntity.ok("Despesa excluída com sucesso: " + id);
    }
}