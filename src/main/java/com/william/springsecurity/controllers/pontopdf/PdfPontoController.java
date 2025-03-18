package com.william.springsecurity.controllers.pontopdf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.format.TextStyle;

import org.springframework.http.ResponseEntity;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.william.springsecurity.domain.candidatos.Candidatos;
import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.pontopdf.RegistroPontoPdf;
import com.william.springsecurity.domain.pontopdf.RelatorioPontoDTO;
import com.william.springsecurity.repositories.candidatos.CandidatosRepository;
import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.services.RelatorioPontoService;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.*;

@RestController
public class PdfPontoController {

    @Autowired
    private RelatorioPontoService relatorioPontoService;

    @Autowired
    private InternoRepository internoRepository;

    @Autowired
    private CandidatosRepository candidatosRepository;

    @GetMapping("/ponto/registros/funcionario/pdf")
    public ResponseEntity<byte[]> gerarPdfRegistroFuncionario(
            @RequestParam(required = false) Long funcionarioId,
            @RequestParam int ano,
            @RequestParam int mes) {

        // Caso funcionarioId seja informado: PDF para um único candidato
        if (funcionarioId != null) {
            RelatorioPontoDTO dto = relatorioPontoService.createRelatorioPontoDTO(funcionarioId, ano, mes);

             if (dto.getRegistrosPontos() == null || dto.getRegistrosPontos().isEmpty()) { //apagar para imprimir mesmo sem dados
            // boolean hasAnyRecord = dto.getRegistrosPontos() != null &&
            //         dto.getRegistrosPontos().stream()
            //                 .anyMatch(registro -> registro.getEntrada() != null ||
            //                         registro.getSaidaAlmoco() != null ||
            //                         registro.getRetornoAlmoco() != null ||
            //                         registro.getSaida() != null);

            // if (!hasAnyRecord) {
                try {
                    byte[] pdfBytes;
                    pdfBytes = gerarPdfSemRegistros(funcionarioId, dto.getAno(), dto.getMes(), dto.getProntuario(),
                            dto.getNome(), dto.getFuncao());

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("filename", "registro_ponto_" + funcionarioId + ".pdf");

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(pdfBytes);
                } catch (DocumentException | IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(("Erro ao gerar PDF: " + e.getMessage()).getBytes());
                }

            }

            return gerarPdfUnico(dto);

        } else {

            // Se funcionarioId não for informado, processa multi-candidato conforme
            // ano/mes:
            LocalDate hoje = LocalDate.now();
            List<RelatorioPontoDTO> dtos = new ArrayList<>();
            // LocalDate inicio = LocalDate.of(ano, mes, 1);
            // LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());

            if (mes == hoje.getMonthValue() && ano == hoje.getYear()) {

                // Mês atual: busca candidatos com "trabalha" = "sim"
                List<Candidatos> candidatos = candidatosRepository.findByTrabalhaIgnoreCase("sim");
                if (candidatos.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT)
                            .body("Nenhum candidato encontrado com trabalha = sim.".getBytes());
                }

                for (Candidatos candidato : candidatos) {
                    Interno interno = internoRepository.findByProntuario(candidato.getProntuario())
                            .orElse(null);
                    if (interno == null)
                        continue; // pula se não encontrar o Interno correspondente
                    RelatorioPontoDTO dto = relatorioPontoService.createRelatorioPontoDTO(interno.getId(), ano, mes);
                    dtos.add(dto);
                }

            } else {
                // Mês histórico: busca candidatos cujo campo "trabalhou" não é nulo
                List<Candidatos> candidatos = candidatosRepository.findByTrabalhouIsNotNull();
                for (Candidatos candidato : candidatos) {
                    Optional<Interno> optionalInterno = internoRepository.findByProntuario(candidato.getProntuario());
                    if (!optionalInterno.isPresent())
                        continue;
                    Interno interno = optionalInterno.get();
                    RelatorioPontoDTO dto = relatorioPontoService.createRelatorioPontoDTO(interno.getId(), ano, mes);
                    // Verifica se há algum registro efetivo de ponto
                    boolean hasAnyRecord = dto.getRegistrosPontos() != null &&
                            dto.getRegistrosPontos().stream().anyMatch(registro -> registro.getEntrada() != null ||
                                    registro.getSaidaAlmoco() != null ||
                                    registro.getRetornoAlmoco() != null ||
                                    registro.getSaida() != null);
                    if (hasAnyRecord) {
                        dtos.add(dto);
                    }
                }
            }
            if (dtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("Nenhum registro de ponto encontrado para os candidatos informados.".getBytes());
            }
            return gerarPdfMulti(dtos);
        }
    }

    private ResponseEntity<byte[]> gerarPdfUnico(RelatorioPontoDTO dto) {
        // Gera PDF para o candidato único
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 15, 20, 0);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            gerarPaginaPdf(document, dto);
            document.close();
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro ao gerar PDF: " + e.getMessage()).getBytes());
        }

        byte[] pdfBytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registro_funcionario_mes.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

    }

    private ResponseEntity<byte[]> gerarPdfMulti(List<RelatorioPontoDTO> dtos) {
        // Gera um único PDF com uma página para cada candidato
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 20, 15, 20, 0);
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            boolean firstPage = true;
            for (RelatorioPontoDTO dto : dtos) {
                if (!firstPage) {
                    document.newPage();
                }
                firstPage = false;
                gerarPaginaPdf(document, dto);
            }
            document.close();
        } catch (DocumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro ao gerar PDF: " + e.getMessage()).getBytes());
        }

        byte[] pdfBytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registro_funcionarios_mes.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

    }

    /**
     * Gera o conteúdo de uma página do PDF para um candidato (RelatorioPontoDTO).
     */
    private void gerarPaginaPdf(Document document, RelatorioPontoDTO dto) throws DocumentException, IOException {
        Font boldFont12 = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 12, Font.BOLD,
                BaseColor.BLACK);
        Font normalFont12 = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 10, Font.NORMAL,
                BaseColor.BLACK);

        // --- Cabeçalho com logo e dados institucionais ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(80);
        headerTable.setWidths(new float[] { 1, 3 });
        headerTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

        // Célula do logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/" + dto.getImagem());
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
            logo.scaleToFit(80, 50);
            logo.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(logo);
            logoCell.setPaddingLeft(60f);
            logoCell.setPaddingRight(-20f);
        } catch (IOException | BadElementException e) {
            e.printStackTrace();
        }
        headerTable.addCell(logoCell);

        // Célula do texto institucional
        Paragraph textParagraph = new Paragraph();
        textParagraph.add(new Chunk(dto.getCabecalho1(), boldFont12));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk(dto.getCabecalho2(), normalFont12));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk(dto.getCabecalho3(), normalFont12));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk(dto.getCabecalho4(), normalFont12));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk(dto.getCabecalho5(), normalFont12));
        PdfPCell textCell = new PdfPCell(textParagraph);
        textCell.setVerticalAlignment(Element.ALIGN_CENTER);
        textCell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
        textCell.setPaddingLeft(60f);
        textCell.setBorder(PdfPCell.NO_BORDER);
        headerTable.addCell(textCell);
        document.add(headerTable);

        // Título centralizado
        Paragraph pHeader = new Paragraph(new Chunk(dto.getTitulo(), boldFont12).setUnderline(0.5f, -2f));
        pHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(pHeader);

        // Cabeçalho com dados do candidato
        PdfPTable headerTable2 = new PdfPTable(2);
        headerTable2.setWidthPercentage(100);
        headerTable2.setWidths(new float[] { 2, 2 });
        headerTable2.getDefaultCell().setBorder(PdfPCell.RECTANGLE);

        // Primeira célula: Nome, Função e Localização
        Paragraph textParagraph2 = new Paragraph();
        textParagraph2.add(Chunk.NEWLINE);
        textParagraph2.add(new Chunk("Nome: ", boldFont12));
        textParagraph2.add(new Chunk(dto.getNome(), normalFont12));
        textParagraph2.add(Chunk.NEWLINE);
        textParagraph2.add(new Chunk("Função: ", boldFont12));
        textParagraph2.add(new Chunk(dto.getFuncao() != null ? dto.getFuncao() : "", normalFont12));
        textParagraph2.add(Chunk.NEWLINE);
        textParagraph2.add(new Chunk("Localização: ", boldFont12));
        textParagraph2
                .add(new Chunk(dto.getUltimaLocalizacao() != null ? dto.getUltimaLocalizacao() : "", normalFont12));
        PdfPCell cellHeader1 = new PdfPCell();
        cellHeader1.setBorder(PdfPCell.NO_BORDER);
        cellHeader1.addElement(textParagraph2);
        cellHeader1.setVerticalAlignment(Element.ALIGN_LEFT);
        headerTable2.addCell(cellHeader1);

        // Segunda célula: Mês/Ano, Prontuário, Mãe, Regime e Unidade
        Paragraph textParagraph3 = new Paragraph();
        textParagraph3.add(Chunk.NEWLINE);
        textParagraph3.add(new Chunk("Mês/Ano: ", boldFont12));
        String monthName = Month.of(dto.getMes()).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        textParagraph3.add(new Chunk(monthName + " / " + dto.getAno(), normalFont12));
        textParagraph3.add(Chunk.NEWLINE);
        textParagraph3.add(new Chunk("Prontuário: ", boldFont12));
        textParagraph3.add(new Chunk(dto.getProntuario(), normalFont12));
        textParagraph3.add(Chunk.NEWLINE);
        textParagraph3.add(new Chunk("Mãe: ", boldFont12));
        textParagraph3.add(new Chunk(dto.getMae() != null ? dto.getMae() : "", normalFont12));
        textParagraph3.add(Chunk.NEWLINE);
        PdfPCell cellHeader2 = new PdfPCell();
        cellHeader2.setBorder(PdfPCell.NO_BORDER);
        cellHeader2.addElement(textParagraph3);
        cellHeader2.setVerticalAlignment(Element.ALIGN_LEFT);
        headerTable2.addCell(cellHeader2);

        document.add(headerTable2);

        // --- Tabela dos registros diários ---
        PdfPTable table = new PdfPTable(new float[] { 1, 1, 3, 2, 2 });
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        addTableHeader(table, boldFont12);

        Duration totalHorasMes = Duration.ZERO;
        List<RegistroPontoPdf> registrosOrdenados = dto.getRegistrosPontos();

        for (RegistroPontoPdf registro : registrosOrdenados) {

            table.addCell(centeredCell(String.valueOf(registro.getDayOfMonth()), registro.isWeekend(), normalFont12));

            DayOfWeek dayOfWeek = registro.getDayOfWeek();
            String dayName = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"));
            dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);
            table.addCell(centeredCell(dayName, registro.isWeekend(), normalFont12));

            if (registro != null) {
                List<String> times = Arrays.asList(
                        registro.getEntrada() != null ? registro.getEntrada().toString() : "",
                        registro.getSaidaAlmoco() != null ? registro.getSaidaAlmoco().toString() : "",
                        registro.getRetornoAlmoco() != null ? registro.getRetornoAlmoco().toString() : "",
                        registro.getSaida() != null ? registro.getSaida().toString() : "");
                table.addCell(centeredCell(String.join(" | ", times), registro.isWeekend(), normalFont12));
                table.addCell(centeredCell(formatDuration(calcularHorasTrabalhadas(registro)), registro.isWeekend(),
                        normalFont12));
                table.addCell(centeredCell(registro.getObservacao(), registro.isWeekend(), normalFont12));

                Duration horasTrabalhadas = calcularHorasTrabalhadas(registro);
                totalHorasMes = totalHorasMes.plus(horasTrabalhadas);
            }
        }
        document.add(table);

        document.add(
                new Paragraph(new Chunk("Horas Trabalhadas no Mês: " + formatDuration(totalHorasMes), normalFont12)));

        // --- Rodapé ---
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));

        Paragraph stateStamp2 = new Paragraph("________________________________________________________",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
        stateStamp2.setAlignment(Element.ALIGN_CENTER);
        document.add(stateStamp2);

        Paragraph stateStamp = new Paragraph("Responsável - " + dto.getUnidade() + " / ADMINISTRAÇÃO",
                boldFont12);
        stateStamp.setAlignment(Element.ALIGN_CENTER);
        document.add(stateStamp);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        Paragraph pGenerated = new Paragraph("Gerado em: " +
                new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss").format(new Date()),
                normalFont12);
        pGenerated.setAlignment(Element.ALIGN_RIGHT);
        document.add(pGenerated);

    }

    private byte[] gerarPdfSemRegistros(Long funcionarioId, int ano, int mes, String prontuario, String nome,
            String funcao) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        Font boldFont12 = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 12, Font.BOLD,
                BaseColor.BLACK);
        Font normalFont12 = FontFactory.getFont("Calibri", BaseFont.CP1252, BaseFont.EMBEDDED, 12, Font.NORMAL,
                BaseColor.BLACK);

        document.add(new Chunk("Relatório de Frequência", boldFont12).setUnderline(0.5f, -2f));
        document.add(new Paragraph("\n"));
        document.add(new Chunk("Prontuário: ", boldFont12));
        document.add(new Chunk(prontuario, normalFont12));
        document.add(Chunk.NEWLINE);
        document.add(new Chunk("Nome: ", boldFont12));
        document.add(new Chunk(nome, normalFont12));
        document.add(Chunk.NEWLINE);
        document.add(new Chunk("Função: ", boldFont12));
        document.add(new Chunk(funcao != null ? funcao : "", normalFont12));
        document.add(Chunk.NEWLINE);

        String monthName = Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"));
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        document.add(new Chunk("Período: ", boldFont12));
        document.add(new Chunk(monthName + " / " + ano, normalFont12));
        document.add(new Paragraph("\n"));

        Font warningFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.RED);
        document.add(new Paragraph("⚠️ Nenhum registro de ponto encontrado para este período.", warningFont));

        document.close();

        return outputStream.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font boldFont12) {
        BaseColor headerColor = BaseColor.DARK_GRAY;
        Stream.of("Dia", "Dia da Semana", "Registros dos Pontos", "Horas Trabalhadas", "Observação")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(headerColor);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, boldFont12));
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private PdfPCell centeredCell(String text, boolean isWeekend, Font normalFont12) {
        PdfPCell cell = new PdfPCell(new Phrase(text, normalFont12));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (isWeekend) {
            cell.setBackgroundColor(BaseColor.GREEN);
        }
        return cell;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private Duration calcularHorasTrabalhadas(RegistroPontoPdf registro) {
        List<LocalTime> pontos = new ArrayList<>();

        if (registro.getEntrada() != null) {
            pontos.add(registro.getEntrada());
        }
        if (registro.getSaidaAlmoco() != null) {
            pontos.add(registro.getSaidaAlmoco());
        }
        if (registro.getRetornoAlmoco() != null) {
            pontos.add(registro.getRetornoAlmoco());
        }
        if (registro.getSaida() != null) {
            pontos.add(registro.getSaida());
        }

        // Se há pelo menos 2 registros, o cálculo é válido
        if (pontos.size() >= 2) {
            if (pontos.size() == 2 || pontos.size() == 3) {
                // Para 2 ou 3 batidas, consideramos somente o intervalo entre o primeiro e o
                // segundo ponto
                return Duration.between(pontos.get(0), pontos.get(1));
            } else if (pontos.size() == 4) {
                Duration periodoManha = Duration.between(pontos.get(0), pontos.get(1));
                Duration periodoTarde = Duration.between(pontos.get(2), pontos.get(3));
                return periodoManha.plus(periodoTarde);
            }
        }

        return Duration.ZERO;
    }

}
