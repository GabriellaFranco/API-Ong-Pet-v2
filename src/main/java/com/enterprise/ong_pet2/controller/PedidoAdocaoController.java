package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.enums.StatusAdocao;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoRequestDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoResponseDTO;
import com.enterprise.ong_pet2.model.dto.pedido_adocao.PedidoAdocaoUpdateDTO;
import com.enterprise.ong_pet2.service.PedidoAdocaoService;
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
public class PedidoAdocaoController {

    private final PedidoAdocaoService pedidoAdocaoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<PedidoAdocaoResponseDTO>> getAllPedidos(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var pedidos = pedidoAdocaoService.getAllPedidos(pageable);
        return pedidos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pedidos);
    }

    @GetMapping("/results")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<Page<PedidoAdocaoResponseDTO>> getPedidosByFilter(
            @RequestParam(required = false) StatusAdocao status,
            @RequestParam(required = false) LocalDate dataPedido,
            @RequestParam(required = false) String adotante,
            @RequestParam(required = false) String voluntario,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        var pedidos = pedidoAdocaoService.getPedidosByFilter(status, dataPedido, adotante, voluntario, pageable);
        return pedidos.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoAdocaoResponseDTO> getPedidoById(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoAdocaoService.getPedidoById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PADRAO')")
    public ResponseEntity<PedidoAdocaoResponseDTO> createPedido(
            @Valid @RequestBody PedidoAdocaoRequestDTO dto) {
        var pedido = pedidoAdocaoService.createPedido(dto);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(pedido.id()).toUri();
        return ResponseEntity.created(uri).body(pedido);
    }

    @PatchMapping("/analise/{id}")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public ResponseEntity<PedidoAdocaoResponseDTO> updateStatusPedido(
            @PathVariable Long id,
            @Valid @RequestBody PedidoAdocaoUpdateDTO dto) {
        return ResponseEntity.ok(pedidoAdocaoService.updateStatusPedido(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePedido(@PathVariable Long id) {
        pedidoAdocaoService.deletePedido(id);
        return ResponseEntity.ok("Pedido excluído com sucesso: " + id);
    }
}