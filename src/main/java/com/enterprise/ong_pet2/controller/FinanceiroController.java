package com.enterprise.ong_pet2.controller;

import com.enterprise.ong_pet2.model.dto.financeiro.ResumoFinanceiroDTO;
import com.enterprise.ong_pet2.service.FinanceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/financeiro")
@Tag(name = "Financeiro", description = "Resumo financeiro e exportação de relatórios")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    @Operation(
            summary = "Resumo financeiro",
            description = "Retorna total de doações, despesas, saldo e breakdown por categoria. " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso")
            }
    )
    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<ResumoFinanceiroDTO> getResumo(
            @Parameter(description = "Data início (formato: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data fim (formato: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return ResponseEntity.ok(financeiroService.getResumo(dataInicio, dataFim));
    }

    @Operation(
            summary = "Exportar relatório CSV",
            description = "Gera arquivo CSV com doações, despesas ou ambos. " +
                    "tipo: DOACOES, DESPESAS ou COMPLETO (padrão). " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso")
            }
    )
    @GetMapping("/relatorio/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<StreamingResponseBody> exportarCsv(
            @Parameter(description = "Tipo: DOACOES, DESPESAS ou COMPLETO")
            @RequestParam(defaultValue = "COMPLETO") String tipo,
            @Parameter(description = "Data início (formato: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data fim (formato: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        StreamingResponseBody body = outputStream ->
                financeiroService.exportarCsv(tipo, dataInicio, dataFim, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-financeiro.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(body);
    }

    @Operation(
            summary = "Exportar relatório PDF",
            description = "Gera arquivo PDF com tabelas formatadas de doações e/ou despesas. " +
                    "tipo: DOACOES, DESPESAS ou COMPLETO (padrão). " +
                    "Requer role ADMIN ou VOLUNTARIO.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Arquivo gerado com sucesso")
            }
    )
    @GetMapping("/relatorio/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResponseEntity<StreamingResponseBody> exportarPdf(
            @Parameter(description = "Tipo: DOACOES, DESPESAS ou COMPLETO")
            @RequestParam(defaultValue = "COMPLETO") String tipo,
            @Parameter(description = "Data início (formato: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data fim (formato: yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        StreamingResponseBody body = outputStream ->
                financeiroService.exportarPdf(tipo, dataInicio, dataFim, outputStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=relatorio-financeiro.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }
}