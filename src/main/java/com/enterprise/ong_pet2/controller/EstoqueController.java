package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueResponseDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoResponseDTO;
import com.enterprise.ong_pet2.service.EstoqueService;
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
@RequestMapping("/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping("/itens")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<ItemEstoqueResponseDTO>> getItens(
            @RequestParam(required = false) CategoriaEstoque categoria,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean alertaEstoqueBaixo,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var itens = estoqueService.getItensByFilter(categoria, ativo, alertaEstoqueBaixo, pageable);
        return itens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(itens);
    }

    @GetMapping("/itens/alerta")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<ItemEstoqueResponseDTO>> getItensComEstoqueBaixo(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var itens = estoqueService.getItensComEstoqueBaixo(pageable);
        return itens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(itens);
    }

    @PostMapping("/itens")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<ItemEstoqueResponseDTO> createItem(
            @Valid @RequestBody ItemEstoqueRequestDTO dto) {
        var item = estoqueService.createItem(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(item.id()).toUri();
        return ResponseEntity.created(uri).body(item);
    }

    @PostMapping("/itens/{id}/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<MovimentacaoResponseDTO> registrarMovimentacao(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequestDTO dto) {
        return ResponseEntity.ok(estoqueService.registrarMovimentacao(id, dto));
    }

    @GetMapping("/itens/{id}/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> getMovimentacoes(
            @PathVariable Long id,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) LocalDateTime dataInicio,
            @RequestParam(required = false) LocalDateTime dataFim,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var movimentacoes = estoqueService.getMovimentacoesByItem(id, tipo, dataInicio, dataFim, pageable);
        return movimentacoes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(movimentacoes);
    }
}