package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.TipoDoacao;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.doacao.DoacaoResponseDTO;
import com.enterprise.ong_pet2.service.DoacaoService;
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
public class DoacaoController {

    private final DoacaoService doacaoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DoacaoResponseDTO>> getAllDoacoes(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var doacoes = doacaoService.getAllDoacoes(pageable);
        return doacoes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(doacoes);
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DoacaoResponseDTO>> getDoacoesByFilter(
            @RequestParam(required = false) String doador,
            @RequestParam(required = false) TipoDoacao categoria,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var doacoes = doacaoService.getDoacoesByFilter(doador, categoria, dataInicio, dataFim, pageable);
        return doacoes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(doacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoacaoResponseDTO> getDoacaoById(@PathVariable Long id) {
        return ResponseEntity.ok(doacaoService.getDoacaoById(id));
    }

    @PostMapping
    public ResponseEntity<DoacaoResponseDTO> createDoacao(@Valid @RequestBody DoacaoRequestDTO dto) {
        var doacao = doacaoService.createDoacao(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(doacao.id()).toUri();
        return ResponseEntity.created(uri).body(doacao);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PADRAO')")
    public ResponseEntity<String> deleteDoacao(@PathVariable Long id) {
        doacaoService.deleteDoacao(id);
        return ResponseEntity.ok("Doação excluída com sucesso: " + id);
    }
}