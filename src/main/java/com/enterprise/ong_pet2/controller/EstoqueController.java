package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.CategoriaEstoque;
import com.enterprise.ong_pet2.enums.TipoMovimentacao;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.ItemEstoqueResponseDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.estoque.MovimentacaoResponseDTO;
import com.enterprise.ong_pet2.service.EstoqueService;
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
@RequestMapping("/estoque")
@Tag(name = "Estoque", description = "Controle de estoque de itens recebidos como doação")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @Operation(
            summary = "Listar itens do estoque",
            description = "Retorna itens com saldo atual. Filtra por categoria, status ativo e alerta de estoque baixo. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum item encontrado")
            }
    )
    @GetMapping("/itens")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<ItemEstoqueResponseDTO>> getItens(
            @Parameter(description = "Categoria: RACAO, MEDICAMENTO, ACESSORIO, HIGIENE ou OUTRO")
            @RequestParam(required = false) CategoriaEstoque categoria,
            @Parameter(description = "Filtrar por itens ativos: true ou false")
            @RequestParam(required = false) Boolean ativo,
            @Parameter(description = "Retornar apenas itens com estoque abaixo do mínimo: true")
            @RequestParam(required = false) Boolean alertaEstoqueBaixo,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var itens = estoqueService.getItensByFilter(categoria, ativo, alertaEstoqueBaixo, pageable);
        return itens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(itens);
    }

    @Operation(
            summary = "Itens com estoque baixo",
            description = "Atalho para itens ativos com quantidade atual abaixo do mínimo configurado. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum item com estoque baixo")
            }
    )
    @GetMapping("/itens/alerta")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<ItemEstoqueResponseDTO>> getItensComEstoqueBaixo(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var itens = estoqueService.getItensComEstoqueBaixo(pageable);
        return itens.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(itens);
    }

    @Operation(
            summary = "Cadastrar item no estoque",
            description = "Cadastra um novo item no estoque com unidade de medida e quantidade mínima. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Item cadastrado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos")
            }
    )
    @PostMapping("/itens")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<ItemEstoqueResponseDTO> createItem(
            @Valid @RequestBody ItemEstoqueRequestDTO dto) {
        var item = estoqueService.createItem(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(item.id()).toUri();
        return ResponseEntity.created(uri).body(item);
    }

    @Operation(
            summary = "Registrar movimentação",
            description = "Registra entrada ou saída do estoque. " +
                    "Saídas validam saldo suficiente. " +
                    "AJUSTE_INVENTARIO requer role ADMIN. " +
                    "Pode ser vinculado a uma doação ou animal.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Movimentação registrada com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Item, doação ou animal não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Saldo insuficiente ou item inativo")
            }
    )
    @PostMapping("/itens/{id}/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<MovimentacaoResponseDTO> registrarMovimentacao(
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoRequestDTO dto) {
        return ResponseEntity.ok(estoqueService.registrarMovimentacao(id, dto));
    }

    @Operation(
            summary = "Histórico de movimentações",
            description = "Retorna o histórico completo de movimentações de um item, " +
                    "com filtros por tipo e período. Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhuma movimentação encontrada"),
                    @ApiResponse(responseCode = "404", description = "Item não encontrado")
            }
    )
    @GetMapping("/itens/{id}/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<MovimentacaoResponseDTO>> getMovimentacoes(
            @PathVariable Long id,
            @Parameter(description = "Tipo: ENTRADA ou SAIDA")
            @RequestParam(required = false) TipoMovimentacao tipo,
            @Parameter(description = "Data início (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) LocalDateTime dataInicio,
            @Parameter(description = "Data fim (formato: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) LocalDateTime dataFim,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var movimentacoes = estoqueService.getMovimentacoesByItem(
                id, tipo, dataInicio, dataFim, pageable);
        return movimentacoes.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(movimentacoes);
    }
}