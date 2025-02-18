package com.william.springsecurity.controllers.ponto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.ponto.RegistroPonto;
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

    // Novo endpoint para gerar PDF dos registros de um determinado mês
    // Exemplo de URL: GET /ponto/registros/mes/pdf?ano=2025&mes=2
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

            // Crie a tabela de cabeçalho com 2 colunas
PdfPTable headerTable = new PdfPTable(2);
headerTable.setWidthPercentage(100);
// Ajuste as larguras relativas das colunas (por exemplo, 1 para o logo e 3 para o texto)
headerTable.setWidths(new float[]{1, 3});
// Remova as bordas padrão das células
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
    logo.scaleToFit(100, 50); // ajuste o tamanho conforme necessário
    logo.setAlignment(Element.ALIGN_CENTER);
    logoCell.addElement(logo);
} catch (IOException | BadElementException e) {
    e.printStackTrace();
}
headerTable.addCell(logoCell);

// Célula do texto
// Defina as fontes: em negrito para "CEARÁ" e normal para os demais
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

// Adicione a tabela de cabeçalho ao documento
document.add(headerTable);

            List<RegistroPonto> registros = entry.getValue();
            Interno interno = registros.get(0).getInterno();

            Duration totalHorasMes = Duration.ZERO;

            Paragraph pHeader = new Paragraph("Controle de Trabalho do Interno", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK));
            pHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(pHeader);

            //Header com dados
            PdfPTable headerTable2 = new PdfPTable(2);
            headerTable2.setWidthPercentage(100);
            headerTable2.setWidths(new float[]{2, 2});
            headerTable2.getDefaultCell().setBorder(PdfPCell.RECTANGLE);

            Font boldFont2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont2 = FontFactory.getFont(FontFactory.HELVETICA, 10);
            
            Paragraph textParagraph2 = new Paragraph();
            textParagraph2.add(Chunk.NEWLINE);
            textParagraph2.add(new Chunk("Nome: ", boldFont2));
            textParagraph2.add(new Chunk(interno.getNome(), normalFont2));
            textParagraph2.add(Chunk.NEWLINE);
            textParagraph2.add(new Chunk("Funcao: ", boldFont2));
            textParagraph2.add(new Chunk(interno.getFuncao(), normalFont2));
            textParagraph2.add(Chunk.NEWLINE);
            textParagraph2.add(new Chunk("Localização: ", boldFont2));
            textParagraph2.add(new Chunk(interno.getLocalizacao(), normalFont2));

            PdfPCell cellHeader1 = new PdfPCell();
            cellHeader1.setBorder(PdfPCell.NO_BORDER);
            cellHeader1.addElement(textParagraph2);
            cellHeader1.setVerticalAlignment(Element.ALIGN_LEFT);
            
            headerTable2.addCell(cellHeader1);

            Paragraph textParagraph3 = new Paragraph();
            textParagraph3.add(Chunk.NEWLINE);
            textParagraph3.add(new Chunk("Mês/Ano: ", boldFont2));
            textParagraph3.add(new Chunk(Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-BR"))+ " / " + ano , normalFont2));
            textParagraph3.add(Chunk.NEWLINE);
            textParagraph3.add(new Chunk("Prontuário: ", boldFont2));
            textParagraph3.add(new Chunk(interno.getProntuario(), normalFont2));
            textParagraph3.add(Chunk.NEWLINE);
            textParagraph3.add(new Chunk("Mãe: ", boldFont2));
            textParagraph3.add(new Chunk(interno.getMae(), normalFont2));
            
            PdfPCell textCell2 = new PdfPCell();
            textCell2.setBorder(PdfPCell.NO_BORDER);
            textCell2.addElement(textParagraph3);
            textCell2.setVerticalAlignment(Element.ALIGN_LEFT);
            
            headerTable2.addCell(textCell2);

            document.add(headerTable2);

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
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" ")); 

            Paragraph stateStamp2 = new Paragraph("__________________________________________", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
            stateStamp2.setAlignment(Element.ALIGN_CENTER);  // Defina o alinhamento depois
            document.add(stateStamp2);

            Paragraph stateStamp = new Paragraph("Responsável - UP - SOBRAL / ADMINISTRAÇÃO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK));
            stateStamp.setAlignment(Element.ALIGN_CENTER);  // Defina o alinhamento depois
            document.add(stateStamp);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Paragraph pGenerated = new Paragraph("Gerado em: " + new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss").format(new Date()), 
            FontFactory.getFont(FontFactory.HELVETICA, 10));
            pGenerated.setAlignment(Element.ALIGN_RIGHT); // Define o alinhamento
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


private Duration calcularHorasTrabalhadas(RegistroPonto registro) {
    if (registro.getEntrada() != null && registro.getSaida() != null) {
        return Duration.between(registro.getEntrada(), registro.getSaida());
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
    public ResponseEntity<byte[]> gerarPdfFuncionarioMes(
        @RequestParam Long funcionarioId,
        @RequestParam int ano,
        @RequestParam int mes) {

    // Busca os registros do funcionário para o mês informado
    List<RegistroPonto> registros = registroPontoService.listarRegistrosPorFuncionarioMes(funcionarioId, ano, mes);
    if (registros.isEmpty()) {
        return ResponseEntity.noContent().build();
    }
    
    Interno interno = registros.get(0).getInterno();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Document document = new Document();
    try {
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(new Paragraph("Funcionário: " + interno.getNome(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Mês/Ano: " + Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + " / " + ano));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        table.addCell("Dia da Semana");
        table.addCell("Dia");
        table.addCell("Registros dos Pontos");
        table.addCell("Horas Trabalhadas");
        table.addCell("Observação");

        Duration totalHorasMes = Duration.ZERO;
        for (RegistroPonto registro : registros) {
            String diaAbreviado = registro.getDia().getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            String diaNumero = String.valueOf(registro.getDia().getDayOfMonth());
            table.addCell(diaAbreviado);
            table.addCell(diaNumero);

            List<String> times = new ArrayList<>();
            if (registro.getEntrada() != null) times.add(registro.getEntrada().toString());
            if (registro.getSaidaAlmoco() != null) times.add(registro.getSaidaAlmoco().toString());
            if (registro.getRetornoAlmoco() != null) times.add(registro.getRetornoAlmoco().toString());
            if (registro.getSaida() != null) times.add(registro.getSaida().toString());

            String registrosPonto;
            if (times.size() == 4) {
                registrosPonto = times.get(0) + " | " + times.get(1) + " / " + times.get(2) + " | " + times.get(3);
            } else {
                registrosPonto = String.join(" | ", times);
            }
            table.addCell(registrosPonto);

            String horasTrabalhadasStr = "";
            Duration horasTrabalhadas = null;
            if (times.size() == 4) {
                Duration periodo1 = Duration.between(registro.getEntrada(), registro.getSaidaAlmoco());
                Duration periodo2 = Duration.between(registro.getRetornoAlmoco(), registro.getSaida());
                horasTrabalhadas = periodo1.plus(periodo2);
            } else if (times.size() >= 2) {
                LocalTime inicio = registro.getEntrada();
                LocalTime fim = registro.getSaida() != null ? registro.getSaida() : registro.getSaidaAlmoco();
                horasTrabalhadas = Duration.between(inicio, fim);
            }
            if (horasTrabalhadas != null) {
                horasTrabalhadasStr = formatDuration(horasTrabalhadas);
                totalHorasMes = totalHorasMes.plus(horasTrabalhadas);
            }
            table.addCell(horasTrabalhadasStr);

            String observacao = "";
            if (times.size() < 2) {
                observacao = "Frequência Incompleta / Carga Horária Incompleta";
            } else if (times.size() < 4) {
                observacao = "Frequência Incompleta";
            } else if (times.size() == 4 && !horasTrabalhadas.equals(Duration.ofHours(8))) {
                observacao = "Carga Horária Incompleta";
            }
            table.addCell(observacao);
        }
        document.add(table);

        // Dados do funcionário (footer)
        document.add(new Paragraph("Função: " + interno.getFuncao()));
        document.add(new Paragraph("Localização: " + interno.getLocalizacao()));
        document.add(new Paragraph("Prontuário: " + interno.getProntuario()));
        document.add(new Paragraph("Mãe: " + interno.getMae()));
        document.add(new Paragraph("Horas Trabalhadas no Mês: " + formatDuration(totalHorasMes)));
        document.add(new Paragraph("Gerado em: " + new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss").format(new Date())));
        
        document.close();
    } catch (DocumentException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    byte[] pdfBytes = baos.toByteArray();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(ContentDisposition.builder("attachment").filename("registros_funcionario.pdf").build());
    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
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
