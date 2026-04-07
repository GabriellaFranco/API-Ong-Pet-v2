package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.StatusAdocao;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoResponseDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoUpdateDTO;
import com.enterprise.ong_pet2.service.PedidoAdocaoService;
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
@RequestMapping("/pedidos-adocao")
@Tag(name = "Pedidos de Adoção", description = "Solicitação e gestão de pedidos de adoção")
public class PedidoAdocaoController {

    private final PedidoAdocaoService pedidoAdocaoService;

    @Operation(
            summary = "Listar todos os pedidos",
            description = "Retorna todos os pedidos paginados. Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum pedido encontrado"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão")
            }
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<PedidoAdocaoResponseDTO>> getAllPedidos(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var pedidos = pedidoAdocaoService.getAllPedidos(pageable);
        return pedidos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pedidos);
    }

    @Operation(
            summary = "Buscar pedidos por filtro",
            description = "Filtra pedidos por status, data, adotante ou voluntário. Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resultado retornado com sucesso"),
                    @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado")
            }
    )
    @GetMapping("/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<PedidoAdocaoResponseDTO>> getPedidosByFilter(
            @Parameter(description = "Status: SOLICITADA, EM_ANALISE, APROVADA, REPROVADA ou CONCLUIDA")
            @RequestParam(required = false) StatusAdocao status,
            @Parameter(description = "Data do pedido (formato: yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate dataPedido,
            @Parameter(description = "Nome do adotante")
            @RequestParam(required = false) String adotante,
            @Parameter(description = "Nome do voluntário responsável")
            @RequestParam(required = false) String voluntario,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var pedidos = pedidoAdocaoService.getPedidosByFilter(
                status, dataPedido, adotante, voluntario, pageable);
        return pedidos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pedidos);
    }

    @Operation(
            summary = "Buscar pedido por ID",
            description = "Retorna um pedido pelo ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<PedidoAdocaoResponseDTO> getPedidoById(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoAdocaoService.getPedidoById(id));
    }

    @Operation(
            summary = "Criar pedido de adoção",
            description = "Cria um pedido de adoção para o animal informado. " +
                    "Requer role PADRAO. Limite de 3 pedidos pendentes por usuário. " +
                    "O voluntário com menor carga de trabalho é atribuído automaticamente.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "422", description = "Animal indisponível, pedido duplicado ou limite atingido")
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('PADRAO')")
    public ResponseEntity<PedidoAdocaoResponseDTO> createPedido(
            @Valid @RequestBody PedidoAdocaoRequestDTO dto) {
        var pedido = pedidoAdocaoService.createPedido(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(pedido.id()).toUri();
        return ResponseEntity.created(uri).body(pedido);
    }

    @Operation(
            summary = "Atualizar status do pedido",
            description = "Atualiza o status de um pedido. Requer role VOLUNTARIO. " +
                    "Transições válidas: SOLICITADA → EM_ANALISE ou REPROVADA, " +
                    "EM_ANALISE → APROVADA ou REPROVADA, APROVADA → CONCLUIDA.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Transição de status inválida")
            }
    )
    @PatchMapping("/analise/{id}")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public ResponseEntity<PedidoAdocaoResponseDTO> updateStatusPedido(
            @PathVariable Long id,
            @Valid @RequestBody PedidoAdocaoUpdateDTO dto) {
        return ResponseEntity.ok(pedidoAdocaoService.updateStatusPedido(id, dto));
    }

    @Operation(
            summary = "Excluir pedido",
            description = "Remove um pedido pelo ID. Requer role ADMIN. " +
                    "Só é possível excluir pedidos com status SOLICITADA.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pedido excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
                    @ApiResponse(responseCode = "422", description = "Pedido já avaliado não pode ser excluído")
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePedido(@PathVariable Long id) {
        pedidoAdocaoService.deletePedido(id);
        return ResponseEntity.ok("Pedido excluído com sucesso: " + id);
    }
}