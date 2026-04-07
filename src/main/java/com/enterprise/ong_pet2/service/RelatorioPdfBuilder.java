package com.enterprise.ong_pet2.service;

import com.enterprise.ong_pet2.entity.Despesa;
import com.enterprise.ong_pet2.entity.Doacao;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RelatorioPdfBuilder {

    private String titulo;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private List<Doacao> doacoes = List.of();
    private List<Despesa> despesas = List.of();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DeviceRgb COR_HEADER = new DeviceRgb(27, 79, 114);
    private static final DeviceRgb COR_LINHA_PAR = new DeviceRgb(214, 234, 248);

    public RelatorioPdfBuilder comTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public RelatorioPdfBuilder comPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        return this;
    }

    public RelatorioPdfBuilder comDoacoes(List<Doacao> doacoes) {
        this.doacoes = doacoes;
        return this;
    }

    public RelatorioPdfBuilder comDespesas(List<Despesa> despesas) {
        this.despesas = despesas;
        return this;
    }

    public void build(OutputStream outputStream) throws IOException {
        var pdf = new PdfDocument(new PdfWriter(outputStream));
        var doc = new Document(pdf);

        // Título
        doc.add(new Paragraph(titulo)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(COR_HEADER));

        // Período
        String periodo = "Período: ";
        if (dataInicio != null) periodo += dataInicio.format(FMT);
        if (dataInicio != null && dataFim != null) periodo += " a ";
        if (dataFim != null) periodo += dataFim.format(FMT);
        if (dataInicio == null && dataFim == null) periodo += "Todos os registros";

        doc.add(new Paragraph(periodo)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY));

        doc.add(new Paragraph(" "));

        // Resumo
        if (!doacoes.isEmpty() || !despesas.isEmpty()) {
            adicionarResumo(doc);
        }

        // Tabela de doações
        if (!doacoes.isEmpty()) {
            doc.add(new Paragraph("Doações")
                    .setFontSize(13)
                    .setBold()
                    .setFontColor(COR_HEADER)
                    .setMarginTop(10));
            adicionarTabelaDoacoes(doc);
        }

        // Tabela de despesas
        if (!despesas.isEmpty()) {
            doc.add(new Paragraph("Despesas")
                    .setFontSize(13)
                    .setBold()
                    .setFontColor(COR_HEADER)
                    .setMarginTop(10));
            adicionarTabelaDespesas(doc);
        }

        doc.close();
    }

    private void adicionarResumo(Document doc) {
        var totalDoacoes = doacoes.stream()
                .map(Doacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalDespesas = despesas.stream()
                .map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var saldo = totalDoacoes.subtract(totalDespesas);

        var tabela = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth();

        tabela.addCell(celulaHeader("Total Doações"));
        tabela.addCell(celulaHeader("Total Despesas"));
        tabela.addCell(celulaHeader("Saldo"));

        tabela.addCell(celulaValor("R$ " + totalDoacoes, false));
        tabela.addCell(celulaValor("R$ " + totalDespesas, false));
        tabela.addCell(celulaValor("R$ " + saldo, false));

        doc.add(tabela);
        doc.add(new Paragraph(" "));
    }

    private void adicionarTabelaDoacoes(Document doc) {
        var tabela = new Table(UnitValue.createPercentArray(new float[]{1, 2, 2, 2, 2, 2}))
                .useAllAvailableWidth();

        tabela.addCell(celulaHeader("ID"));
        tabela.addCell(celulaHeader("Data"));
        tabela.addCell(celulaHeader("Doador"));
        tabela.addCell(celulaHeader("Valor (R$)"));
        tabela.addCell(celulaHeader("Categoria"));
        tabela.addCell(celulaHeader("Animal"));

        for (int i = 0; i < doacoes.size(); i++) {
            var d = doacoes.get(i);
            boolean par = i % 2 == 0;
            tabela.addCell(celulaValor(String.valueOf(d.getId()), par));
            tabela.addCell(celulaValor(d.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), par));
            tabela.addCell(celulaValor(d.getDoador().getNome(), par));
            tabela.addCell(celulaValor(d.getValor().toString(), par));
            tabela.addCell(celulaValor(d.getCategoria().name(), par));
            tabela.addCell(celulaValor(d.getAnimal() != null ? d.getAnimal().getNome() : "-", par));
        }

        doc.add(tabela);
    }

    private void adicionarTabelaDespesas(Document doc) {
        var tabela = new Table(UnitValue.createPercentArray(new float[]{1, 2, 3, 2, 2, 2}))
                .useAllAvailableWidth();

        tabela.addCell(celulaHeader("ID"));
        tabela.addCell(celulaHeader("Data"));
        tabela.addCell(celulaHeader("Descrição"));
        tabela.addCell(celulaHeader("Valor (R$)"));
        tabela.addCell(celulaHeader("Categoria"));
        tabela.addCell(celulaHeader("Animal"));

        for (int i = 0; i < despesas.size(); i++) {
            var d = despesas.get(i);
            boolean par = i % 2 == 0;
            tabela.addCell(celulaValor(String.valueOf(d.getId()), par));
            tabela.addCell(celulaValor(d.getData().format(FMT), par));
            tabela.addCell(celulaValor(d.getDescricao(), par));
            tabela.addCell(celulaValor(d.getValor().toString(), par));
            tabela.addCell(celulaValor(d.getCategoria().name(), par));
            tabela.addCell(celulaValor(d.getAnimal() != null ? d.getAnimal().getNome() : "-", par));
        }

        doc.add(tabela);
    }

    private Cell celulaHeader(String texto) {
        return new Cell()
                .add(new Paragraph(texto).setBold().setFontColor(ColorConstants.WHITE).setFontSize(9))
                .setBackgroundColor(COR_HEADER)
                .setPadding(5);
    }

    private Cell celulaValor(String texto, boolean linhaAlternada) {
        var cell = new Cell()
                .add(new Paragraph(texto != null ? texto : "-").setFontSize(9))
                .setPadding(4);
        if (linhaAlternada) {
            cell.setBackgroundColor(COR_LINHA_PAR);
        }
        return cell;
    }
}