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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;

@RestController
public class CandidatosController {

    @Autowired
    private CandidatosRepository candidatosRepository;

    @PostMapping("/planilha/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo está vazio");
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.badRequest().body("Formato de arquivo inválido. Somente .xlsx é permitido.");
        }

        // Conjunto para registrar os prontuários atualizados via planilha
        Set<String> prontuariosAtualizados = new HashSet<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                return ResponseEntity.badRequest().body("Arquivo sem dados.");
            }

            // Lê o cabeçalho e mapeia as colunas (normalizando para minúsculas e sem espaços extras)
            Row headerRow = rowIterator.next();
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                columnIndexMap.put(header, cell.getColumnIndex());
            }

            // Detecta o layout: se a coluna "unidade" existir, assume-se Layout 2; senão, Layout 1.
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
            // Formatter para o campo "trabalhou" no formato "MM-yyyy"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String prontuario = getCellValue(row.getCell(columnIndexMap.get("prontuário")));
                // Registra o prontuário para saber que esse candidato foi atualizado na planilha
                prontuariosAtualizados.add(prontuario);

                String nome = getCellValue(row.getCell(columnIndexMap.get("nome")));
                String mae = getCellValue(row.getCell(columnIndexMap.get("mãe")));
                String ultimaLocalizacao = getCellValue(row.getCell(columnIndexMap.get("última localização")));

                String unidade;
                String funcao;
                String tipoDeRegime;
                String trabalha = null;
                String trabalhou = null;

                if (isLayout2) {
                    // Layout 2: usa as colunas "unidade" e "tipo de regime".
                    unidade = getCellValue(row.getCell(columnIndexMap.get("unidade")));
                    tipoDeRegime = getCellValue(row.getCell(columnIndexMap.get("tipo de regime")));
                    funcao = null; // Não há coluna para função neste layout
                } else {
                    // Layout 1: não há coluna "unidade", então usa o valor padrão.
                    unidade = "UP - SOBRAL";
                    funcao = getCellValue(row.getCell(columnIndexMap.get("função/cargo")));
                    tipoDeRegime = getCellValue(row.getCell(columnIndexMap.get("regime")));
                    // Para Layout 1, define "trabalha" como "sim" e "trabalhou" como o mês/ano atual.
                    trabalha = "sim";
                    trabalhou = now.format(formatter);
                }

                // O campo "biometria" não existe na planilha, portanto é sempre null.
                String biometria = null;

                Optional<Candidatos> optionalCandidato = candidatosRepository.findByProntuario(prontuario);
                Candidatos candidato = optionalCandidato.orElseGet(Candidatos::new);
                candidato.setProntuario(prontuario);
                candidato.setNome(nome);
                candidato.setMae(mae);
                candidato.setUnidade(unidade);
                candidato.setUltimaLocalizacao(ultimaLocalizacao);
                candidato.setTipoDeRegime(tipoDeRegime);
                candidato.setFuncao(funcao);
                candidato.setBiometria(biometria);
                candidato.setDataDaAtualizacao(now);

                // Se o layout for 1, atualizamos os campos trabalha e trabalhou conforme definidos na planilha.
                if (!isLayout2) {
                    candidato.setTrabalha(trabalha);
                    candidato.setTrabalhou(trabalhou);
                }

                candidatosRepository.save(candidato);
            }

            // Agora, para os candidatos que NÃO foram atualizados via planilha, atualiza o campo "trabalha" para "nao".
            // Se o candidato já tinha "sim", adiciona uma vírgula e o valor do mês/ano atual em "trabalhou".
            List<Candidatos> todosCandidatos = candidatosRepository.findAll();
            for (Candidatos cand : todosCandidatos) {
                if (!prontuariosAtualizados.contains(cand.getProntuario())) {
                    // Se o campo "trabalha" estava com "sim", acrescenta no "trabalhou"
                    if ("sim".equalsIgnoreCase(cand.getTrabalha())) {
                        String atual = cand.getTrabalhou();
                        String novoValor = now.format(formatter);
                        if (atual == null || atual.isEmpty()) {
                            cand.setTrabalhou(novoValor);
                        } else {
                            cand.setTrabalhou(atual + "," + novoValor);
                        }
                    }
                    cand.setTrabalha("nao");
                    candidatosRepository.save(cand);
                }
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
                    // Converte valores numéricos para string sem o ".0"
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
    @GetMapping("/planilha/candidatos/{prontuario}")
    public ResponseEntity<?> getCandidatoByProntuario(@PathVariable String prontuario) {
        Optional<Candidatos> optionalCandidato = candidatosRepository.findByProntuario(prontuario);
        if (optionalCandidato.isPresent()) {
            return ResponseEntity.ok(optionalCandidato.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/candidatos/oldest-data")
    public ResponseEntity<?> getOldestDataAtualizacao() {
    Optional<Candidatos> candidateOpt = candidatosRepository.findTopByOrderByDataDaAtualizacaoAsc();
    if (candidateOpt.isPresent()) {
        LocalDateTime oldestData = candidateOpt.get().getDataDaAtualizacao();
        // Retorna em formato JSON, ex: { "oldestData": "2025-02-18T14:15:53.841" }
        return ResponseEntity.ok(Collections.singletonMap("oldestData", oldestData));
    }
    return ResponseEntity.ok(Collections.singletonMap("oldestData", null));
    }

    @GetMapping("/candidatos/statistics")
    public ResponseEntity<?> getCandidatosStatistics() {
    // Obtém o total de candidatos com trabalha = 'sim'
    long totalTrabalhaSim = candidatosRepository.countByTrabalha("sim");

    // Obtém o total de candidatos com trabalha = 'sim' e biometria = 'sim'
    long totalTrabalhaSimBiometriaSim = candidatosRepository.countByTrabalhaAndBiometria("sim", "sim");

    // Consulta os valores distintos de funcao e suas contagens
    List<Object[]> funcoesRaw = candidatosRepository.countFuncaoGroupByFuncao();
    List<Map<String, Object>> funcoes = new ArrayList<>();
    for (Object[] row : funcoesRaw) {
        Map<String, Object> map = new HashMap<>();
        map.put("funcao", row[0]);  // valor do campo funcao
        map.put("count", row[1]);   // quantidade de ocorrências
        funcoes.add(map);
    }

    // Monta o objeto de resposta
    Map<String, Object> response = new HashMap<>();
    response.put("totalTrabalhaSim", totalTrabalhaSim);
    response.put("totalTrabalhaSimBiometriaSim", totalTrabalhaSimBiometriaSim);
    response.put("funcoes", funcoes);

    return ResponseEntity.ok(response);
    }


}
