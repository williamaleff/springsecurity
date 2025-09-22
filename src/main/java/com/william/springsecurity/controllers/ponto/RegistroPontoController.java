package com.william.springsecurity.controllers.ponto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.services.RegistroPontoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    // Endpoint para listar registros de um determinado mês agrupados por
    // funcionário
    // Exemplo de chamada: GET /ponto/registros/mes?ano=2025&mes=2
    @GetMapping("/registros/mes")
    public ResponseEntity<Map<Long, List<RegistroPonto>>> listarRegistrosPorMes(
            @RequestParam int ano,
            @RequestParam int mes) {
        Map<Long, List<RegistroPonto>> registros = registroPontoService.listarRegistrosAgrupadosPorFuncionarioMes(ano,
                mes);
        return ResponseEntity.ok(registros);
    }

    // Novo endpoint para obter o total de horas trabalhadas por um funcionário em
    // um determinado mês
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


    // Endpoint para gerar o arquivo Excel com os registros de um funcionário para
    // um determinado mês
    // Exemplo de URL: GET
    // /ponto/registros/funcionario/excel?funcionarioId=123&ano=2025&mes=2
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
                // Verifica se os valores não são nulos, convertendo para String quando
                // necessário
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
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("registros_funcionario_" + funcionarioId + "_" + mes + "_" + ano + ".xlsx")
                .build());

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    // Endpoint para gerar o Excel com registros de um determinado mês agrupados por
    // funcionário
    // Exemplo de URL: GET /ponto/registros/mes/excel?ano=2025&mes=2
    @GetMapping("/registros/mes/excel")
    public ResponseEntity<byte[]> gerarExcelMes(
            @RequestParam int ano,
            @RequestParam int mes,
            @RequestParam(required = false) String funcao) {

        // Obtém os registros agrupados por funcionário para o mês informado
        Map<Long, List<RegistroPonto>> registrosAgrupados = registroPontoService
                .listarRegistrosAgrupadosPorFuncionarioMes(ano, mes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            
                 // Cria uma única planilha
                Sheet sheet = workbook.createSheet("Registros");

                // Cria a linha de cabeçalho
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Funcionário ID");
        headerRow.createCell(1).setCellValue("ID");
        headerRow.createCell(2).setCellValue("Data");
        headerRow.createCell(3).setCellValue("Dia Semana");
        headerRow.createCell(4).setCellValue("Entrada");
        headerRow.createCell(5).setCellValue("Saída Almoço");
        headerRow.createCell(6).setCellValue("Retorno Almoço");
        headerRow.createCell(7).setCellValue("Saída");
        headerRow.createCell(8).setCellValue("Observação");

                // Insere os registros na planilha
                int rowIdx = 1;
                for (Map.Entry<Long, List<RegistroPonto>> entry : registrosAgrupados.entrySet()) {
                Long funcionarioId = entry.getKey();
                List<RegistroPonto> registros = entry.getValue();
            
                for (RegistroPonto rp : registros) {
                    Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(funcionarioId);
                row.createCell(1).setCellValue(rp.getId() != null ? rp.getId() : 0);
                row.createCell(2).setCellValue(rp.getDia() != null ? rp.getDia().toString() : "");
                row.createCell(3).setCellValue(rp.getDiaSemana() != null ? rp.getDiaSemana() : "");
                row.createCell(4).setCellValue(rp.getEntrada() != null ? rp.getEntrada().toString() : "");
                row.createCell(5).setCellValue(rp.getSaidaAlmoco() != null ? rp.getSaidaAlmoco().toString() : "");
                row.createCell(6).setCellValue(rp.getRetornoAlmoco() != null ? rp.getRetornoAlmoco().toString() : "");
                row.createCell(7).setCellValue(rp.getSaida() != null ? rp.getSaida().toString() : "");
                row.createCell(8).setCellValue(rp.getObservacao() != null ? rp.getObservacao() : "");                
            }
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
