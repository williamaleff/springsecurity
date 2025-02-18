package com.william.springsecurity.controllers.candidatos;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.william.springsecurity.domain.candidatos.Candidatos;
import com.william.springsecurity.repositories.candidatos.CandidatosRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/planilha")
public class CandidatosController {

    @Autowired
    private CandidatosRepository candidatosRepository;

    @PostMapping("/upload")
public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("Arquivo está vazio");
    }

    if (!file.getOriginalFilename().endsWith(".xlsx")) {
        return ResponseEntity.badRequest().body("Formato de arquivo inválido. Somente .xlsx é permitido.");
    }

    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        if (!rowIterator.hasNext()) {
            return ResponseEntity.badRequest().body("Arquivo sem dados.");
        }

        // Lê o cabeçalho e mapeia as colunas
        Row headerRow = rowIterator.next();
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim().toLowerCase();
            columnIndexMap.put(header, cell.getColumnIndex());
        }

        // Detecta o layout: se a coluna "unidade" existir, assumimos Layout 2; senão, Layout 1.
        boolean isLayout2 = columnIndexMap.containsKey("unidade");

        // Validação dos cabeçalhos obrigatórios conforme o layout
        if (isLayout2) {
            String[] requiredColumns = {"prontuário", "nome", "mãe", "unidade", "última localização", "tipo de regime"};
            for (String col : requiredColumns) {
                if (!columnIndexMap.containsKey(col)) {
                    return ResponseEntity.badRequest().body("Coluna obrigatória não encontrada: " + col);
                }
            }
        } else {
            String[] requiredColumns = {"prontuário", "nome", "mãe", "última localização", "função/cargo", "regime"};
            for (String col : requiredColumns) {
                if (!columnIndexMap.containsKey(col)) {
                    return ResponseEntity.badRequest().body("Coluna obrigatória não encontrada: " + col);
                }
            }
        }
        
        LocalDateTime now = LocalDateTime.now();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            String prontuario = getCellValue(row.getCell(columnIndexMap.get("prontuário")));
            String nome = getCellValue(row.getCell(columnIndexMap.get("nome")));
            String mae = getCellValue(row.getCell(columnIndexMap.get("mãe")));
            String ultimaLocalizacao = getCellValue(row.getCell(columnIndexMap.get("última localização")));
        
            String unidade;
            String funcao;
            String tipoDeRegime;
        
            if (isLayout2) {
                // Layout 2: usamos a coluna "unidade" e "tipo de regime". Ignoramos a coluna "biometria".
                unidade = getCellValue(row.getCell(columnIndexMap.get("unidade")));
                tipoDeRegime = getCellValue(row.getCell(columnIndexMap.get("tipo de regime")));
                funcao = null; // Não há coluna para função neste layout
            } else {
                // Layout 1: não há coluna "unidade", então usamos valor padrão, e a coluna "função/cargo" e "regime"
                unidade = "UP - SOBRAL";
                funcao = getCellValue(row.getCell(columnIndexMap.get("função/cargo")));
                tipoDeRegime = getCellValue(row.getCell(columnIndexMap.get("regime")));
            }

            Optional<Candidatos> optionalCandidato = candidatosRepository.findByProntuario(prontuario);
            Candidatos candidato = optionalCandidato.orElseGet(Candidatos::new);
            candidato.setProntuario(prontuario);
            candidato.setNome(nome);
            candidato.setMae(mae);
            candidato.setUnidade(unidade);
            candidato.setUltimaLocalizacao(ultimaLocalizacao);
            candidato.setTipoDeRegime(tipoDeRegime);
            candidato.setFuncao(funcao);
            candidato.setDataDaAtualizacao(now);

            candidatosRepository.save(candidato);
        }
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo.");
    }

    return ResponseEntity.ok("Arquivo processado com sucesso");
}
    /**
     * Método auxiliar para extrair o valor de uma célula do Excel.
     */
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Força a conversão numérica para string SEM o .0
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
     /**
     * Endpoint para buscar um candidato pelo prontuário.
     * Exemplo de URL: GET /planilha/candidatos/12345
     */
    @GetMapping("/candidatos/{prontuario}")
    public ResponseEntity<?> getCandidatoByProntuario(@PathVariable String prontuario) {
        Optional<Candidatos> optionalCandidato = candidatosRepository.findByProntuario(prontuario);
        if (optionalCandidato.isPresent()) {
            return ResponseEntity.ok(optionalCandidato.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
