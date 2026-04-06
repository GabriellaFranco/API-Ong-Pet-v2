package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Despesa;
import com.enterprise.ong_pet2.entity.Doacao;
import com.enterprise.ong_pet2.enums.TipoDoacao;
import com.enterprise.ong_pet2.model.dto.financeiro.ResumoFinanceiroDTO;
import com.enterprise.ong_pet2.repository.DespesaRepository;
import com.enterprise.ong_pet2.repository.DoacaoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceiroService {

    private final DoacaoRepository doacaoRepository;
    private final DespesaRepository despesaRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public ResumoFinanceiroDTO getResumo(LocalDate dataInicio, LocalDate dataFim) {
        var doacoes = buscarDoacoes(null, null, dataInicio, dataFim);
        var despesas = buscarDespesas(null, dataInicio, dataFim, null);

        var totalDoacoes = doacoes.stream()
                .map(Doacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalDespesas = despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<TipoDoacao, BigDecimal> doacoesPorCategoria = doacoes.stream()
                .collect(Collectors.groupingBy(
                        Doacao::getCategoria,
                        Collectors.reducing(BigDecimal.ZERO, Doacao::getValor, BigDecimal::add)
                ));

        return new ResumoFinanceiroDTO(
                totalDoacoes,
                totalDespesas,
                totalDoacoes.subtract(totalDespesas),
                doacoesPorCategoria,
                doacoes.size(),
                despesas.size()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public void exportarCsv(String tipo, LocalDate dataInicio, LocalDate dataFim,
                            OutputStream outputStream) throws IOException {

        try (var writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             var printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setSkipHeaderRecord(false).build()
             )) {

            switch (tipo.toUpperCase()) {
                case "DOACOES" -> exportarDoacoesCsv(printer, dataInicio, dataFim);
                case "DESPESAS" -> exportarDespesasCsv(printer, dataInicio, dataFim);
                default -> {
                    exportarDoacoesCsv(printer, dataInicio, dataFim);
                    exportarDespesasCsv(printer, dataInicio, dataFim);
                }
            }
        }
    }

    private void exportarDoacoesCsv(CSVPrinter printer, LocalDate dataInicio,
                                    LocalDate dataFim) throws IOException {
        printer.printRecord("ID", "Data", "Doador", "Valor (R$)", "Categoria", "Descrição", "Animal");
        for (var doacao : buscarDoacoes(null, null, dataInicio, dataFim)) {
            printer.printRecord(
                    doacao.getId(),
                    doacao.getData().format(FORMATTER),
                    doacao.getDoador().getNome(),
                    doacao.getValor(),
                    doacao.getCategoria(),
                    doacao.getDescricao() != null ? doacao.getDescricao() : "",
                    doacao.getAnimal() != null ? doacao.getAnimal().getNome() : ""
            );
        }
    }

    private void exportarDespesasCsv(CSVPrinter printer, LocalDate dataInicio,
                                     LocalDate dataFim) throws IOException {
        printer.printRecord("ID", "Data", "Descrição", "Valor (R$)", "Categoria", "Animal", "Responsável");
        for (var despesa : buscarDespesas(null, dataInicio, dataFim, null)) {
            printer.printRecord(
                    despesa.getId(),
                    despesa.getData().format(DATE_FORMATTER),
                    despesa.getDescricao(),
                    despesa.getValor(),
                    despesa.getCategoria(),
                    despesa.getAnimal() != null ? despesa.getAnimal().getNome() : "",
                    despesa.getResponsavel().getNome()
            );
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VOLUNTARIO')")
    public void exportarPdf(String tipo, LocalDate dataInicio, LocalDate dataFim,
                            OutputStream outputStream) throws IOException {

        var doacoes = tipo.equalsIgnoreCase("DESPESAS")
                ? List.<Doacao>of()
                : buscarDoacoes(null, null, dataInicio, dataFim);

        var despesas = tipo.equalsIgnoreCase("DOACOES")
                ? List.<Despesa>of()
                : buscarDespesas(null, dataInicio, dataFim, null);

        new RelatorioPdfBuilder()
                .comTitulo("Relatório Financeiro — ONG Pet")
                .comPeriodo(dataInicio, dataFim)
                .comDoacoes(doacoes)
                .comDespesas(despesas)
                .build(outputStream);
    }

    private List<Doacao> buscarDoacoes(String doador, TipoDoacao categoria,
                                       LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio != null
                ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null
                ? dataFim.atTime(LocalTime.MAX) : null;

        return doacaoRepository
                .findByFilter(doador, categoria, inicio, fim,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();
    }

    private List<Despesa> buscarDespesas(
            com.enterprise.ong_pet2.enums.CategoriaEstoque categoria,
            LocalDate dataInicio, LocalDate dataFim, Long idAnimal) {
        return despesaRepository
                .findByFilter(categoria, dataInicio, dataFim, idAnimal,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();
    }
}