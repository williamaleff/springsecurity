package com.william.springsecurity.controllers.ponto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.william.springsecurity.domain.candidatos.Candidatos;
import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.repositories.candidatos.CandidatosRepository;
import com.william.springsecurity.services.RegistroPontoService;
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
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;

import org.apache.poi.ss.usermodel.Sheet;

@RestController
@RequestMapping("/ponto")
public class RegistroPontoController {

    @Autowired
    private RegistroPontoService registroPontoService;

    @Autowired
    private CandidatosRepository candidatosRepository;

    @PostMapping("/registrar/{funcionarioId}")
    public ResponseEntity<RegistroPontoResponseDTO> registrarPonto(@PathVariable Long funcionarioId) {
        RegistroPonto registroAtualizado = registroPontoService.registrarPonto(funcionarioId);
        RegistroPontoResponseDTO responseDTO = RegistroPontoResponseDTO.from(registroAtualizado);
        return ResponseEntity.ok(responseDTO);
    }

    // Novo endpoint para listar registros agrupados por funcionário
    @GetMapping("/registros")
    public ResponseEntity<Map<Long, List<RegistroPonto>>> listarRegistrosAgrupadosPorFuncionario() {
        Map<Long, List<RegistroPonto>> registros = registroPontoService.listarRegistrosAgrupadosPorFuncionario();
        return ResponseEntity.ok(registros);
    }

    // Endpoint para listar registros de um determinado mês agrupados por funcionário
    // Exemplo de chamada: GET /ponto/registros/mes?ano=2025&mes=2
    @GetMapping("/registros/mes")
    public ResponseEntity<Map<Long, List<RegistroPonto>>> listarRegistrosPorMes(
            @RequestParam int ano,
            @RequestParam int mes) {
        Map<Long, List<RegistroPonto>> registros = registroPontoService.listarRegistrosAgrupadosPorFuncionarioMes(ano, mes);
        return ResponseEntity.ok(registros);
    }

@GetMapping("pdf/registros/mes")
public ResponseEntity<byte[]> gerarPdfRegistrosMes(@RequestParam int ano, @RequestParam int mes) {
    Map<Long, List<RegistroPonto>> registrosAgrupados = registroPontoService.listarRegistrosAgrupadosPorFuncionarioMes(ano, mes);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 36, 36, 54, 36);

    try {
        PdfWriter.getInstance(document, baos);
        document.open();

        for (Map.Entry<Long, List<RegistroPonto>> entry : registrosAgrupados.entrySet()) {
            document.newPage();

            // Cabeçalho com logo e texto institucional
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1, 3});
            headerTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

            // Logo
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(PdfPCell.NO_BORDER);
            try {
                InputStream is = getClass().getResourceAsStream("/imagens/logo.png");
                if (is == null) {
                    throw new FileNotFoundException("Imagem não encontrada no classpath!");
                }
                byte[] imageBytes = is.readAllBytes();
                Image logo = Image.getInstance(imageBytes);
                logo.scaleToFit(100, 50);
                logo.setAlignment(Element.ALIGN_CENTER);
                logoCell.addElement(logo);
            } catch (IOException | BadElementException e) {
                e.printStackTrace();
            }
            headerTable.addCell(logoCell);

            // Texto institucional
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph textParagraph = new Paragraph();
            textParagraph.add(new Chunk("CEARÁ", boldFont));
            textParagraph.add(Chunk.NEWLINE);
            textParagraph.add(new Chunk("GOVERNO DO ESTADO", normalFont));
            textParagraph.add(Chunk.NEWLINE);
            textParagraph.add(new Chunk("SECRETARIA DA ADMINISTRAÇÃO", normalFont));
            textParagraph.add(Chunk.NEWLINE);
            textParagraph.add(new Chunk("PENITENCIÁRIA E RESSOCIALIZAÇÃO", normalFont));
            PdfPCell textCell = new PdfPCell(textParagraph);
            textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            textCell.setBorder(PdfPCell.NO_BORDER);
            headerTable.addCell(textCell);

            document.add(headerTable);

            List<RegistroPonto> registros = entry.getValue();
            // Obtém o Interno a partir do primeiro registro
            Interno interno = registros.get(0).getInterno();
            
            // Busca o candidato pelo prontuário e verifica se o campo 'trabalha' é "sim"
            Optional<Candidatos> candidatoOpt = candidatosRepository.findByProntuario(interno.getProntuario());
            if (!candidatoOpt.isPresent() || !"sim".equalsIgnoreCase(candidatoOpt.get().getTrabalha())) {
                // Se não atender à condição, ignora esse grupo
                continue;
            }
            Candidatos candidato = candidatoOpt.get();
            
            Duration totalHorasMes = Duration.ZERO;

            Paragraph pHeader = new Paragraph("Controle de Trabalho do Interno", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK));
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            // Cabeçalho com dados do Interno (dados que estão na tabela Interno)
            // e dados de Candidatos (informações que estão na tabela Candidatos)
            PdfPTable headerTable2 = new PdfPTable(2);
            headerTable2.setWidthPercentage(100);
            headerTable2.setWidths(new float[]{2, 2});
            headerTable2.getDefaultCell().setBorder(PdfPCell.RECTANGLE);

            Font boldFont2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont2 = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Primeira célula: dados do Interno e de Candidatos (primeira parte)
            Paragraph textParagraph2 = new Paragraph();
            textParagraph2.add(Chunk.NEWLINE);
            textParagraph2.add(new Chunk("Nome: ", boldFont2));
            textParagraph2.add(new Chunk(interno.getNome(), normalFont2));
            textParagraph2.add(Chunk.NEWLINE);
            textParagraph2.add(new Chunk("Função: ", boldFont2));
            textParagraph2.add(new Chunk(candidato.getFuncao() != null ? candidato.getFuncao() : "", normalFont2));
            textParagraph2.add(Chunk.NEWLINE);
            textParagraph2.add(new Chunk("Localização: ", boldFont2));
            // Se candidato tiver informação, usa a última localização, senão, usa a localização de Interno
            textParagraph2.add(new Chunk(
                    candidato.getUltimaLocalizacao() != null ? candidato.getUltimaLocalizacao() : "", 
                    normalFont2));

            PdfPCell cellHeader1 = new PdfPCell();
            cellHeader1.setBorder(PdfPCell.NO_BORDER);
            cellHeader1.addElement(textParagraph2);
            cellHeader1.setVerticalAlignment(Element.ALIGN_LEFT);
            headerTable2.addCell(cellHeader1);

            // Segunda célula: mais dados combinados
            Paragraph textParagraph3 = new Paragraph();
            textParagraph3.add(Chunk.NEWLINE);
            textParagraph3.add(new Chunk("Mês/Ano: ", boldFont2));
            textParagraph3.add(new Chunk(Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR")) 
                    + " / " + ano, normalFont2));
            textParagraph3.add(Chunk.NEWLINE);
            textParagraph3.add(new Chunk("Prontuário: ", boldFont2));
            textParagraph3.add(new Chunk(interno.getProntuario(), normalFont2));
            textParagraph3.add(Chunk.NEWLINE);
            textParagraph3.add(new Chunk("Mãe: ", boldFont2));
            textParagraph3.add(new Chunk(candidato.getMae() != null ? candidato.getMae() : "", normalFont2));
            textParagraph3.add(Chunk.NEWLINE);
            // textParagraph3.add(new Chunk("Regime: ", boldFont2));
            // textParagraph3.add(new Chunk(candidato.getTipoDeRegime() != null ? candidato.getTipoDeRegime() : "", normalFont2));
            // textParagraph3.add(Chunk.NEWLINE);
            // textParagraph3.add(new Chunk("Unidade: ", boldFont2));
            // textParagraph3.add(new Chunk(candidato.getUnidade() != null ? candidato.getUnidade() : "", normalFont2));

            PdfPCell textCell2 = new PdfPCell();
            textCell2.setBorder(PdfPCell.NO_BORDER);
            textCell2.addElement(textParagraph3);
            textCell2.setVerticalAlignment(Element.ALIGN_LEFT);
            headerTable2.addCell(textCell2);

            document.add(headerTable2);

            // Criação da tabela de registros diários
            PdfPTable table = new PdfPTable(new float[]{1, 1, 4, 1, 2});
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableHeader(table);

            LocalDate startDate = LocalDate.of(ano, mes, 1);
            LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                final LocalDate currentDate = date;
                boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
                RegistroPonto registro = registros.stream()
                        .filter(r -> r.getDia().equals(currentDate))
                        .findFirst()
                        .orElse(null);

                table.addCell(centeredCell(String.valueOf(date.getDayOfMonth()), isWeekend));
                table.addCell(centeredCell(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")), isWeekend));

                if (registro != null) {
                    List<String> times = Arrays.asList(
                            registro.getEntrada() != null ? registro.getEntrada().toString() : "",
                            registro.getSaidaAlmoco() != null ? registro.getSaidaAlmoco().toString() : "",
                            registro.getRetornoAlmoco() != null ? registro.getRetornoAlmoco().toString() : "",
                            registro.getSaida() != null ? registro.getSaida().toString() : ""
                    );
                    table.addCell(centeredCell(String.join(" | ", times), isWeekend));
                    table.addCell(centeredCell(formatDuration(calcularHorasTrabalhadas(registro)), isWeekend));
                    table.addCell(centeredCell("", isWeekend));

                    Duration horasTrabalhadas = calcularHorasTrabalhadas(registro);
                    totalHorasMes = totalHorasMes.plus(horasTrabalhadas);
                } else {
                    table.addCell(centeredCell("", isWeekend));
                    table.addCell(centeredCell("", isWeekend));
                    table.addCell(centeredCell("", isWeekend));
                }
            }
            document.add(table);
            document.add(new Paragraph("Horas Trabalhadas no Mês: " + formatDuration(totalHorasMes)));

            // Rodapé
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph stateStamp2 = new Paragraph("__________________________________________", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
            stateStamp2.setAlignment(Element.ALIGN_CENTER);
            document.add(stateStamp2);

            Paragraph stateStamp = new Paragraph("Responsável - UP - SOBRAL / ADMINISTRAÇÃO", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
            stateStamp.setAlignment(Element.ALIGN_CENTER);
            document.add(stateStamp);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph pGenerated = new Paragraph("Gerado em: " + new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss").format(new Date()), 
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            pGenerated.setAlignment(Element.ALIGN_RIGHT);
            document.add(pGenerated);
        }
        document.close();
    } catch (DocumentException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    byte[] pdfBytes = baos.toByteArray();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.builder("attachment").filename("registros_mes.pdf").build());
    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
}

private void addTableHeader(PdfPTable table) {
    Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
    BaseColor headerColor = BaseColor.DARK_GRAY;
    Stream.of("Dia", "Dia da Semana", "Registros dos Pontos", "Horas Trabalhadas", "Observação")
        .forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(headerColor);
            header.setBorderWidth(1);
            header.setPhrase(new Phrase(columnTitle, headerFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        });
}

private PdfPCell centeredCell(String text, boolean isWeekend) {
    Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
    PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    if(isWeekend) {
        cell.setBackgroundColor(BaseColor.GREEN);
    }
    return cell;
}

/**
 * Calcula as horas trabalhadas para um dia, conforme:
 * - Se houver 2 ou 3 registros: retorna a diferença entre o primeiro e o segundo registro.
 * - Se houver 4 registros: retorna (saidaAlmoco - entrada) + (saida - retornoAlmoco).
 */
private Duration calcularHorasTrabalhadas(RegistroPonto registro) {
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
            // Para 2 ou 3 batidas, consideramos somente o intervalo entre o primeiro e o segundo ponto
            return Duration.between(pontos.get(0), pontos.get(1));
        } else if (pontos.size() == 4) {
            Duration periodoManha = Duration.between(pontos.get(0), pontos.get(1));
            Duration periodoTarde = Duration.between(pontos.get(2), pontos.get(3));
            return periodoManha.plus(periodoTarde);
        }
    }
    
    return Duration.ZERO;
}

    // Novo endpoint para obter o total de horas trabalhadas por um funcionário em um determinado mês
    // Exemplo de URL: GET /ponto/horas-trabalhadas?funcionarioId=123&ano=2025&mes=2
    @GetMapping("/horas-trabalhadas")
    public ResponseEntity<Map<String, String>> getHorasTrabalhadas(
            @RequestParam Long funcionarioId,
            @RequestParam int ano,
            @RequestParam int mes) {
        
        String totalHoras = registroPontoService.calcularHorasTrabalhadasFuncionarioMes(funcionarioId, ano, mes);
        
        Map<String, String> response = new HashMap<>();
        response.put("funcionarioId", funcionarioId.toString());
        response.put("totalHorasTrabalhadas", totalHoras);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registros/funcionario")
    public ResponseEntity<List<RegistroPonto>> listarRegistrosFuncionarioMes(@RequestParam Long funcionarioId,
                                                                             @RequestParam int ano,
                                                                             @RequestParam int mes) {
    List<RegistroPonto> registros = registroPontoService.listarRegistrosPorFuncionarioMes(funcionarioId, ano, mes);
    return ResponseEntity.ok(registros);
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    

    // Endpoint para gerar PDF com os registros de um funcionário para um determinado mês
    // Exemplo de URL: GET /ponto/registros/funcionario/pdf?funcionarioId=123&ano=2025&mes=2
    @GetMapping("/registros/funcionario/pdf")
 public ResponseEntity<byte[]> gerarPdfRegistroFuncionario(
        @RequestParam Long funcionarioId,
        @RequestParam int ano,
        @RequestParam int mes) {
    
    // Obtém os registros do mês para todos os funcionários
    Map<Long, List<RegistroPonto>> registrosAgrupados = registroPontoService.listarRegistrosAgrupadosPorFuncionarioMes(ano, mes);
    // Filtra os registros do funcionário informado
    List<RegistroPonto> registros = registrosAgrupados.get(funcionarioId);
    if (registros == null || registros.isEmpty()) {
        try{
        byte[] pdfBytes;
        pdfBytes = gerarPdfSemRegistros(funcionarioId, ano, mes);
        HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("filename", "registro_ponto_" + funcionarioId + ".pdf");

    return ResponseEntity.ok()
            .headers(headers)
            .body(pdfBytes);
        }catch(DocumentException | IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(("Erro ao gerar PDF: " + e.getMessage()).getBytes());
        }

    }
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 36, 36, 54, 36);

    try {
        PdfWriter.getInstance(document, baos);
        document.open();

        // --- Cabeçalho com logo e dados institucionais ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3});
        headerTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);

        // Célula do logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        try {
            InputStream is = getClass().getResourceAsStream("/imagens/logo.png");
            if (is == null) {
                throw new FileNotFoundException("Imagem não encontrada no classpath!");
            }
            byte[] imageBytes = is.readAllBytes();
            Image logo = Image.getInstance(imageBytes);
            logo.scaleToFit(100, 50);
            logo.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(logo);
        } catch (IOException | BadElementException e) {
            e.printStackTrace();
        }
        headerTable.addCell(logoCell);

        // Célula do texto institucional
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph textParagraph = new Paragraph();
        textParagraph.add(new Chunk("CEARÁ", boldFont));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk("GOVERNO DO ESTADO", normalFont));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk("SECRETARIA DA ADMINISTRAÇÃO", normalFont));
        textParagraph.add(Chunk.NEWLINE);
        textParagraph.add(new Chunk("PENITENCIÁRIA E RESSOCIALIZAÇÃO", normalFont));
        PdfPCell textCell = new PdfPCell(textParagraph);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.setBorder(PdfPCell.NO_BORDER);
        headerTable.addCell(textCell);
        document.add(headerTable);

        // --- Dados do Interno e Candidatos ---
        // Utiliza o primeiro registro para recuperar o objeto Interno
        Interno interno = registros.get(0).getInterno();
        // Busca os dados complementares do candidato, se existirem
        Optional<Candidatos> candidatoOpt = candidatosRepository.findByProntuario(interno.getProntuario());
        Candidatos candidato = candidatoOpt.orElse(new Candidatos());

        Duration totalHorasMes = Duration.ZERO;

        Paragraph pHeader = new Paragraph("Controle de Trabalho do Interno",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK));
        pHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(pHeader);

        // Tabela com os dados do Interno (da tabela Interno) e dados do Candidatos
        PdfPTable headerTable2 = new PdfPTable(2);
        headerTable2.setWidthPercentage(100);
        headerTable2.setWidths(new float[]{2, 2});
        headerTable2.getDefaultCell().setBorder(PdfPCell.RECTANGLE);

        Font boldFont2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont2 = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Primeira célula: Nome, Função e Localização
        Paragraph textParagraph2 = new Paragraph();
        textParagraph2.add(Chunk.NEWLINE);
        textParagraph2.add(new Chunk("Nome: ", boldFont2));
        textParagraph2.add(new Chunk(interno.getNome(), normalFont2));
        textParagraph2.add(Chunk.NEWLINE);
        textParagraph2.add(new Chunk("Função: ", boldFont2));
        textParagraph2.add(new Chunk(candidato.getFuncao() != null ? candidato.getFuncao() : "", normalFont2));
        textParagraph2.add(Chunk.NEWLINE);
        textParagraph2.add(new Chunk("Localização: ", boldFont2));
        textParagraph2.add(new Chunk(candidato.getUltimaLocalizacao() != null ? candidato.getUltimaLocalizacao() : "", normalFont2));

        PdfPCell cellHeader1 = new PdfPCell();
        cellHeader1.setBorder(PdfPCell.NO_BORDER);
        cellHeader1.addElement(textParagraph2);
        cellHeader1.setVerticalAlignment(Element.ALIGN_LEFT);
        headerTable2.addCell(cellHeader1);

        // Segunda célula: Mês/Ano, Prontuário, Mãe, Regime e Unidade
        Paragraph textParagraph3 = new Paragraph();
        textParagraph3.add(Chunk.NEWLINE);
        textParagraph3.add(new Chunk("Mês/Ano: ", boldFont2));
        textParagraph3.add(new Chunk(Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
                + " / " + ano, normalFont2));
        textParagraph3.add(Chunk.NEWLINE);
        textParagraph3.add(new Chunk("Prontuário: ", boldFont2));
        textParagraph3.add(new Chunk(interno.getProntuario(), normalFont2));
        textParagraph3.add(Chunk.NEWLINE);
        textParagraph3.add(new Chunk("Mãe: ", boldFont2));
        textParagraph3.add(new Chunk(candidato.getMae() != null ? candidato.getMae() : "", normalFont2));
        textParagraph3.add(Chunk.NEWLINE);
        // textParagraph3.add(new Chunk("Regime: ", boldFont2));
        // textParagraph3.add(new Chunk(candidato.getTipoDeRegime() != null ? candidato.getTipoDeRegime() : "", normalFont2));
        // textParagraph3.add(Chunk.NEWLINE);
        // textParagraph3.add(new Chunk("Unidade: ", boldFont2));
        // textParagraph3.add(new Chunk(candidato.getUnidade() != null ? candidato.getUnidade() : "", normalFont2));

        PdfPCell cellHeader2 = new PdfPCell();
        cellHeader2.setBorder(PdfPCell.NO_BORDER);
        cellHeader2.addElement(textParagraph3);
        cellHeader2.setVerticalAlignment(Element.ALIGN_LEFT);
        headerTable2.addCell(cellHeader2);

        document.add(headerTable2);

        // --- Tabela dos registros diários ---
        PdfPTable table = new PdfPTable(new float[]{1, 1, 4, 1, 2});
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        addTableHeader(table);

        LocalDate startDate = LocalDate.of(ano, mes, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                                date.getDayOfWeek() == DayOfWeek.SUNDAY;
            // Procura o registro para o dia corrente
            RegistroPonto registro = registros.stream()
                    .filter(r -> r.getDia().equals(currentDate))
                    .findFirst()
                    .orElse(null);

            table.addCell(centeredCell(String.valueOf(date.getDayOfMonth()), isWeekend));
            table.addCell(centeredCell(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR")), isWeekend));

            if (registro != null) {
                List<String> times = Arrays.asList(
                        registro.getEntrada() != null ? registro.getEntrada().toString() : "",
                        registro.getSaidaAlmoco() != null ? registro.getSaidaAlmoco().toString() : "",
                        registro.getRetornoAlmoco() != null ? registro.getRetornoAlmoco().toString() : "",
                        registro.getSaida() != null ? registro.getSaida().toString() : ""
                );
                table.addCell(centeredCell(String.join(" | ", times), isWeekend));
                table.addCell(centeredCell(formatDuration(calcularHorasTrabalhadas(registro)), isWeekend));
                table.addCell(centeredCell("", isWeekend));

                Duration horasTrabalhadas = calcularHorasTrabalhadas(registro);
                totalHorasMes = totalHorasMes.plus(horasTrabalhadas);
            } else {
                table.addCell(centeredCell("", isWeekend));
                table.addCell(centeredCell("", isWeekend));
                table.addCell(centeredCell("", isWeekend));
            }
        }
        document.add(table);
        document.add(new Paragraph("Horas Trabalhadas no Mês: " + formatDuration(totalHorasMes)));

        // --- Rodapé ---
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        Paragraph stateStamp2 = new Paragraph("__________________________________________",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
        stateStamp2.setAlignment(Element.ALIGN_CENTER);
        document.add(stateStamp2);

        Paragraph stateStamp = new Paragraph("Responsável - UP - SOBRAL / ADMINISTRAÇÃO",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
        stateStamp.setAlignment(Element.ALIGN_CENTER);
        document.add(stateStamp);

        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        Paragraph pGenerated = new Paragraph("Gerado em: " +
                new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss").format(new Date()),
                FontFactory.getFont(FontFactory.HELVETICA, 10));
        pGenerated.setAlignment(Element.ALIGN_RIGHT);
        document.add(pGenerated);

        document.close();
    } catch (DocumentException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    byte[] pdfBytes = baos.toByteArray();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.builder("attachment")
            .filename("registro_funcionario_mes.pdf").build());
    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
}
private byte[] gerarPdfSemRegistros(Long funcionarioId, int ano, int mes) throws DocumentException, IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Document document = new Document();
    PdfWriter.getInstance(document, outputStream);

    document.open();
    
    Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    Font textFont = new Font(Font.FontFamily.HELVETICA, 12);

    document.add(new Paragraph("Relatório de Frequência", titleFont));
    document.add(new Paragraph("\n"));
    document.add(new Paragraph("Funcionário ID: " + funcionarioId, textFont));
    document.add(new Paragraph("Período: " + mes + "/" + ano, textFont));
    document.add(new Paragraph("\n"));
    
    Font warningFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.RED);
    document.add(new Paragraph("⚠️ Nenhum registro de ponto encontrado para este período.", warningFont));

    document.close();
    
    return outputStream.toByteArray();
}

    // Endpoint para gerar o arquivo Excel com os registros de um funcionário para um determinado mês
    // Exemplo de URL: GET /ponto/registros/funcionario/excel?funcionarioId=123&ano=2025&mes=2
    @GetMapping("/registros/funcionario/excel")
    public ResponseEntity<byte[]> gerarExcelFuncionarioMes(
            @RequestParam Long funcionarioId,
            @RequestParam int ano,
            @RequestParam int mes) {
        
        // Busca os registros do funcionário para o período informado
        List<RegistroPonto> registros = registroPontoService.listarRegistrosPorFuncionarioMes(funcionarioId, ano, mes);
        
        // Criação do arquivo Excel utilizando Apache POI
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Registros");
            
            // Cria a linha de cabeçalho
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Data");
            headerRow.createCell(2).setCellValue("Dia Semana");
            headerRow.createCell(3).setCellValue("Entrada");
            headerRow.createCell(4).setCellValue("Saída Almoço");
            headerRow.createCell(5).setCellValue("Retorno Almoço");
            headerRow.createCell(6).setCellValue("Saída");
            headerRow.createCell(7).setCellValue("Observação");
            
            // Preenche as linhas com os registros
            int rowIdx = 1;
            for (RegistroPonto rp : registros) {
                Row row = sheet.createRow(rowIdx++);
                // Verifica se os valores não são nulos, convertendo para String quando necessário
                row.createCell(0).setCellValue(rp.getId() != null ? rp.getId() : 0);
                row.createCell(1).setCellValue(rp.getDia() != null ? rp.getDia().toString() : "");
                row.createCell(2).setCellValue(rp.getDiaSemana() != null ? rp.getDiaSemana() : "");
                row.createCell(3).setCellValue(rp.getEntrada() != null ? rp.getEntrada().toString() : "");
                row.createCell(4).setCellValue(rp.getSaidaAlmoco() != null ? rp.getSaidaAlmoco().toString() : "");
                row.createCell(5).setCellValue(rp.getRetornoAlmoco() != null ? rp.getRetornoAlmoco().toString() : "");
                row.createCell(6).setCellValue(rp.getSaida() != null ? rp.getSaida().toString() : "");
                row.createCell(7).setCellValue(rp.getObservacao() != null ? rp.getObservacao() : "");
            }
            
            // Escreve o conteúdo no ByteArrayOutputStream
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        byte[] excelBytes = baos.toByteArray();
        
        // Configura os cabeçalhos HTTP para o download do arquivo Excel
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registros_funcionario_" + funcionarioId + "_" + mes + "_" + ano + ".xlsx")
                .build());
        
        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    // Endpoint para gerar o Excel com registros de um determinado mês agrupados por funcionário
    // Exemplo de URL: GET /ponto/registros/mes/excel?ano=2025&mes=2
    @GetMapping("/registros/mes/excel")
    public ResponseEntity<byte[]> gerarExcelMes(
            @RequestParam int ano,
            @RequestParam int mes) {

        // Obtém os registros agrupados por funcionário para o mês informado
        Map<Long, List<RegistroPonto>> registrosAgrupados = registroPontoService.listarRegistrosAgrupadosPorFuncionarioMes(ano, mes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            // Para cada funcionário, cria uma nova planilha
            for (Map.Entry<Long, List<RegistroPonto>> entry : registrosAgrupados.entrySet()) {
                Long funcionarioId = entry.getKey();
                List<RegistroPonto> registros = entry.getValue();

                // Cria uma planilha com o nome "Funcionario {id}"
                Sheet sheet = workbook.createSheet("Funcionario " + funcionarioId);

                // Cria a linha de cabeçalho
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("ID");
                headerRow.createCell(1).setCellValue("Data");
                headerRow.createCell(2).setCellValue("Dia Semana");
                headerRow.createCell(3).setCellValue("Entrada");
                headerRow.createCell(4).setCellValue("Saída Almoço");
                headerRow.createCell(5).setCellValue("Retorno Almoço");
                headerRow.createCell(6).setCellValue("Saída");
                headerRow.createCell(7).setCellValue("Observação");

                // Insere os registros na planilha
                int rowIdx = 1;
                for (RegistroPonto rp : registros) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rp.getId() != null ? rp.getId() : 0);
                    row.createCell(1).setCellValue(rp.getDia() != null ? rp.getDia().toString() : "");
                    row.createCell(2).setCellValue(rp.getDiaSemana() != null ? rp.getDiaSemana() : "");
                    row.createCell(3).setCellValue(rp.getEntrada() != null ? rp.getEntrada().toString() : "");
                    row.createCell(4).setCellValue(rp.getSaidaAlmoco() != null ? rp.getSaidaAlmoco().toString() : "");
                    row.createCell(5).setCellValue(rp.getRetornoAlmoco() != null ? rp.getRetornoAlmoco().toString() : "");
                    row.createCell(6).setCellValue(rp.getSaida() != null ? rp.getSaida().toString() : "");
                    row.createCell(7).setCellValue(rp.getObservacao() != null ? rp.getObservacao() : "");
                }
            }
            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        byte[] excelBytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registros_mes_" + mes + "_" + ano + ".xlsx")
                .build());

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

}
