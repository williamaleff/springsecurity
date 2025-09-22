package com.william.springsecurity.controllers.pontoexcel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.repositories.candidatos.CandidatosRepository;
import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.services.RegistroPontoService;

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;

import java.time.format.TextStyle;
import java.util.Locale;

@RestController
@RequestMapping("/registros")
public class ExcelPontoController {

    @Autowired
    private RegistroPontoService registroPontoService;

    @Autowired
    private InternoRepository internoRepository;

    @Autowired
    private CandidatosRepository candidatosRepository;

    // GET /registros/mes/excel?ano=2025&mes=6&funcao=COZINHEIRO
    @GetMapping("/mes/excel")
    public ResponseEntity<byte[]> gerarExcelMes(
            @RequestParam int ano,
            @RequestParam int mes,
            @RequestParam(required = false) String funcao,
            @RequestParam(required = false) Long idFuncionario) {

        Map<Long, List<RegistroPonto>> registrosAgrupados = new HashMap<>();

        if (idFuncionario != null) {
            List<RegistroPonto> registros = registroPontoService.listarRegistrosPorFuncionarioMes(idFuncionario, ano,
                    mes);
            if (!registros.isEmpty()) {
                registrosAgrupados.put(idFuncionario, registros);
            }
        } else {
            registrosAgrupados = registroPontoService
                    .listarRegistrosAgrupadosPorFuncionarioMes(ano, mes, funcao);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {

            // Cria uma única planilha
            Sheet sheet = workbook.createSheet("Registros");

            // Estilo de fonte negrito
            CellStyle boldStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            boldStyle.setFont(headerFont);

            // Cabeçalho
            String[] colunas = { "ID", "Nome", "Prontuario", "Data", "Dia Semana", "Entrada",
                    "Saida Almoço", "Retorno Almoço", "Saida", "Observacao", "Funcao", "Mes/Ano" };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(boldStyle);
            }

            Map<Long, Interno> dadosInternos = new HashMap<>();
            Map<String, String> prontuarioParaFuncao = new HashMap<>();

            for (Long funcionarioId : registrosAgrupados.keySet()) {
                internoRepository.findById(funcionarioId).ifPresent(interno -> {
                    dadosInternos.put(funcionarioId, interno);
                    String prontuario = interno.getProntuario();
                    if (prontuario != null && !prontuario.isEmpty()) {
                        candidatosRepository.findByProntuario(prontuario).ifPresent(candidato -> {
                            prontuarioParaFuncao.put(prontuario, candidato.getFuncao());
                        });
                    }
                });
            }

            DateTimeFormatter dtfData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dtfHora = DateTimeFormatter.ofPattern("HH:mm");

            // Insere os registros na planilha
            int rowIdx = 1;
            for (Map.Entry<Long, List<RegistroPonto>> entry : registrosAgrupados.entrySet()) {
                Long funcionarioId = entry.getKey();
                List<RegistroPonto> registros = entry.getValue();
                Interno interno = dadosInternos.get(funcionarioId);
                String nome = interno != null ? interno.getNome() : "";
                String prontuario = interno != null ? interno.getProntuario() : "";
                String funcaoInterno = prontuarioParaFuncao.getOrDefault(prontuario, "");

                for (RegistroPonto rp : registros) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(funcionarioId);
                    row.createCell(1).setCellValue(nome);
                    row.createCell(2).setCellValue(prontuario);
                    row.createCell(3).setCellValue(rp.getDia() != null ? rp.getDia().format(dtfData) : "");
                    if (rp.getDia() != null) {
                        String diaSemanaPt = rp.getDia().getDayOfWeek().getDisplayName(TextStyle.FULL,
                                new Locale("pt", "BR"));
                        row.createCell(4).setCellValue(diaSemanaPt);
                    } else {
                        row.createCell(4).setCellValue("");
                    }
                    row.createCell(5).setCellValue(rp.getEntrada() != null ? rp.getEntrada().format(dtfHora) : "");
                    row.createCell(6)
                            .setCellValue(rp.getSaidaAlmoco() != null ? rp.getSaidaAlmoco().format(dtfHora) : "");
                    row.createCell(7)
                            .setCellValue(rp.getRetornoAlmoco() != null ? rp.getRetornoAlmoco().format(dtfHora) : "");
                    row.createCell(8).setCellValue(rp.getSaida() != null ? rp.getSaida().format(dtfHora) : "");
                    row.createCell(9).setCellValue(rp.getObservacao() != null ? rp.getObservacao() : "");
                    row.createCell(10).setCellValue(funcaoInterno);
                    row.createCell(11).setCellValue(mes + "/" + ano);
                }
            }

            // Autoajuste das colunas
            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        byte[] excelBytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registros_mes_" + mes + "_" + ano + ".xlsx")
                .build());

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
