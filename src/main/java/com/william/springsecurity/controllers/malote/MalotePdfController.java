package com.william.springsecurity.controllers.malote;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.william.springsecurity.domain.candidatos.Candidatos;
import com.william.springsecurity.domain.malote.Registro;
import com.william.springsecurity.domain.malote.RelatorioDTO;
import com.william.springsecurity.repositories.candidatos.CandidatosRepository;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/pdf")

public class MalotePdfController {

    @Autowired
    private CandidatosRepository candidatosRepository;

    @GetMapping("/gerarMalote")
    public ResponseEntity<byte[]> gerarPdf() {
        FontFactory.register("src/main/resources/fonts/Calibri-regular.ttf", "Calibri");

        RelatorioDTO dto = new RelatorioDTO();
        dto.setCabecalho1("UNIDADE PRISIONAL REGIONAL DE SOBRAL");
        dto.setCabecalho2("Entrega de higiênicos da SAP");
        dto.setCabecalho3("Segunda paga de fevereiro - 12/03");
        dto.setImagem("image.png");

        List<Candidatos> candidatos = candidatosRepository.findAll();
        List<Registro> registros = new ArrayList<Registro>();
        for (Candidatos candidato : candidatos) {
            Registro reg = new Registro();
            reg.setProntuario(candidato.getProntuario());
            reg.setNome(candidato.getNome());
            // Utiliza o campo ultimaLocalizacao como localizacao para o registro
            reg.setLocalizacao(candidato.getUltimaLocalizacao());
            registros.add(reg);
        }
        dto.setRegistros(registros);

        List<Registro> registrosOrdenados = dto.getRegistros();

        // Ordena os registros manualmente no Java 7
        Collections.sort(registrosOrdenados, new Comparator<Registro>() {
            @Override
            public int compare(Registro r1, Registro r2) {
                // Primeiro compara por Localização (ordem alfabética)
                int result = r1.getLocalizacao().compareTo(r2.getLocalizacao());

                // Se a localização for a mesma, ordena pelo Nome (ordem alfabética)
                if (result == 0) {
                    result = r1.getNome().compareTo(r2.getNome());
                }

                return result;
            }
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 15, 15, 5, 5);

        String localizacaoAtual = null;
        PdfPTable table = null; // Declaração fora do laço para manter o escopo
        int count = 1;

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            for (Registro registro : registrosOrdenados) {

                // Se a localização mudou, cria uma nova página
                if (localizacaoAtual == null || !registro.getLocalizacao().equals(localizacaoAtual)) {

                    if (localizacaoAtual != null && !registro.getLocalizacao().equals(localizacaoAtual)) {
                        document.add(table);
                        document.newPage(); // Adiciona uma nova página
                        count = 1;
                    }
                    localizacaoAtual = registro.getLocalizacao();

                    // Adiciona o título da nova seção com o nome da localização
                    Font tituloFont = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 15, Font.BOLD,
                            BaseColor.BLACK);
                    Paragraph titulo = new Paragraph("Localização: " + localizacaoAtual, tituloFont);
                    titulo.setSpacingBefore(10f);
                    titulo.setSpacingAfter(10f);
                    document.add(titulo);
                    //document.add(new Paragraph("\n"));

                    // Cabeçalho com logo e texto institucional
                    PdfPTable headerTable = new PdfPTable(2);
                    headerTable.setWidthPercentage(100);
                    headerTable.setWidths(new float[] { 2, 5 });
                    headerTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

                    // Logo
                    PdfPCell logoCell = new PdfPCell();
                    logoCell.setBorder(PdfPCell.NO_BORDER);
                    logoCell.setPaddingLeft(5f);
                    logoCell.setPaddingRight(-15f);
                    try (InputStream is = getClass().getResourceAsStream("/imagens/" + dto.getImagem())) {
                        if (is == null) {
                            throw new FileNotFoundException("Imagem não encontrada no classpath!");
                        }
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] data = new byte[16384];
                        int nRead;
                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        buffer.flush();
                        byte[] imageBytes = buffer.toByteArray();
                        Image logo = Image.getInstance(imageBytes);
                        logo.scaleToFit(180, 90);
                        // logo.scaleToFit(200, 100); // Aumenta a escala da imagem
                        // logo.scaleAbsolute(200, 100); // Define um tamanho fixo para a imagem
                        logo.setAlignment(Element.ALIGN_CENTER);

                        logoCell.addElement(logo);
                        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        logoCell.setBorder(PdfPCell.NO_BORDER);
                        //logoCell.setFixedHeight(100f); // Ajuste conforme necessário
                    } catch (IOException | BadElementException e) {
                        e.printStackTrace();
                    }
                    headerTable.addCell(logoCell);

                    // Texto institucional
                    Font boldFont = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 20, Font.BOLD,
                            BaseColor.BLACK);
                    Font normalFont = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 20,
                            Font.NORMAL,
                            BaseColor.BLACK);
                    Paragraph textParagraph = new Paragraph();
                    textParagraph.add(new Chunk(dto.getCabecalho1(), boldFont).setUnderline(0.5f, -2f));
                    textParagraph.add(Chunk.NEWLINE);
                    textParagraph.add(new Chunk(dto.getCabecalho2(), normalFont));
                    textParagraph.add(Chunk.NEWLINE);
                    textParagraph.add(new Chunk(dto.getCabecalho3(), normalFont));
                    PdfPCell textCell = new PdfPCell(textParagraph);
                    textCell.setVerticalAlignment(Element.ALIGN_CENTER);
                    textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    textCell.setBorder(PdfPCell.NO_BORDER);
                    // Reduz o padding à esquerda para aproximar do logo
                    textCell.setPaddingRight(5f);
                    textCell.setPaddingLeft(-30f);
                    headerTable.addCell(textCell);

                    document.add(headerTable);

                    float[] tableColWidths = { 1, 2, 4, 3, 5 };
                    table = new PdfPTable(tableColWidths);
                    table.setWidthPercentage(100);
                    table.setSpacingBefore(10f);
                    table.setSpacingAfter(10f);

                    addTableHeader(table);

                }

                // Preenchendo as linhas da tabela
                table.addCell(centeredCell(String.valueOf(count)));
                table.addCell(centeredCell(registro.getProntuario()));
                table.addCell(centeredCell(registro.getNome()));
                table.addCell(centeredCell(registro.getLocalizacao()));
                // Campo "Assinatura" em branco
                table.addCell(new Paragraph(""));
                count++;
            }

            // Adiciona a última tabela criada ao documento
            if (table != null) {
                document.add(table);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] pdfBytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registros_mes.pdf")
                .build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private void addTableHeader(PdfPTable table) {
        Font headerFont = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 12, Font.BOLD,
                BaseColor.BLACK);
        BaseColor headerColor = BaseColor.LIGHT_GRAY;

        Stream.of("", "Prontuário", "Nome", "Localização", "Assinatura")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(headerColor);
                    header.setBorderWidth(1);
                    header.setMinimumHeight(25f);
                    header.setPhrase(new Phrase(columnTitle, headerFont));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private PdfPCell centeredCell(String text) {
        Font cellFont = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 12, Font.NORMAL,
                BaseColor.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(15f);
        return cell;
    }

}
