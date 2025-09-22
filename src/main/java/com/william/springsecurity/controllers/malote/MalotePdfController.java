package com.william.springsecurity.controllers.malote;

import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import org.apache.poi.ss.usermodel.*;

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
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.william.springsecurity.domain.malote.Registro;
import com.william.springsecurity.domain.malote.RelatorioDTO;
import com.william.springsecurity.repositories.malote.MaloteRepository;
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
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import com.william.springsecurity.domain.malote.Malote;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;

@RestController
@RequestMapping("/malote")

public class MalotePdfController {

    @Autowired
    private MaloteRepository maloteRepository;

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @GetMapping("/pdf/gerarMalote")
    public ResponseEntity<byte[]> gerarPdf(@RequestParam int paga, @RequestParam int mes) {
        FontFactory.register("src/main/resources/fonts/Calibri-regular.ttf", "Calibri");

        RelatorioDTO dto = new RelatorioDTO();
        dto.setCabecalho1("UNIDADE PRISIONAL REGIONAL DE SOBRAL");
        dto.setCabecalho2("Entrega de higiênicos da SAP");

        // Traduz número da paga
        String textoPaga = paga == 1 ? "Primeira paga" : "Segunda paga";

        // Traduz número do mês para nome em português
        String nomeMes = Month.of(mes)
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

        int anoAtual = java.time.Year.now().getValue();

        // Define o cabeçalho
        dto.setCabecalho3(String.format("%s - %s de %d", textoPaga, capitalize(nomeMes), anoAtual));

        // dto.setCabecalho3("Primeira paga - Maio de 2025");
        dto.setImagem("image.png");

        List<Malote> candidatos = maloteRepository.findAll();
        List<Registro> registros = new ArrayList<Registro>();
        for (Malote candidato : candidatos) {
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

        Map<String, Integer> contadorPorAla = new HashMap<>();
        String localizacaoAtual = null;
        PdfPTable table = null; // Declaração fora do laço para manter o escopo
        int count = 1;

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            AlaPageNumberHelper pageEventHelper = new AlaPageNumberHelper();
            writer.setPageEvent(pageEventHelper); // Associando o evento de página ao writer

            document.open();

            for (Registro registro : registrosOrdenados) {

                String alaAtual = extrairAla(registro.getLocalizacao());
                // Se a localização mudou, cria uma nova página
                if (localizacaoAtual == null || !registro.getLocalizacao().equals(localizacaoAtual)) {

                    if (localizacaoAtual != null && !registro.getLocalizacao().equals(localizacaoAtual)) {
                        document.add(table);

              // Registra a Ala para a página atual
    pageEventHelper.registerAlaForPage(writer.getPageNumber(), extrairAla(localizacaoAtual));

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
                    // document.add(new Paragraph("\n"));

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
                        // logoCell.setFixedHeight(100f); // Ajuste conforme necessário
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
pageEventHelper.registerAlaForPage(writer.getPageNumber(), extrairAla(localizacaoAtual));

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

    private String extrairAla(String localizacao) {
        // Exemplo: "Bloco 01 - Ala A - Cela 1" -> "Bloco 01 - Ala A"
        if (localizacao == null)
            return "DESCONHECIDA";
        String[] partes = localizacao.split(" - ");
        if (partes.length >= 2) {
            return partes[0] + " - " + partes[1]; // Bloco + Ala
        } else {
            return localizacao; // Usa inteira para casos como "Hospital de..."
        }
    }

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

            // Lê o cabeçalho e mapeia as colunas (normalizando para minúsculas e sem
            // espaços extras)
            Row headerRow = rowIterator.next();
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                columnIndexMap.put(header, cell.getColumnIndex());
            }

            // Detecta o layout: se a coluna "unidade" existir, assume-se Layout 2; senão,
            // Layout 1.
            boolean isLayout2 = columnIndexMap.containsKey("unidade");

            maloteRepository.deleteAll();

            // Validação dos cabeçalhos obrigatórios conforme o layout
            if (isLayout2) {
                String[] requiredColumns = { "prontuário", "nome", "mãe", "unidade", "última localização",
                        "tipo de regime" };
                for (String col : requiredColumns) {
                    if (!columnIndexMap.containsKey(col)) {
                        return ResponseEntity.badRequest().body("Coluna obrigatória não encontrada: " + col);
                    }
                }
            } else {
                String[] requiredColumns = { "prontuário", "nome", "mãe", "última localização", "função/cargo",
                        "regime" };
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
                // Registra o prontuário para saber que esse candidato foi atualizado na
                // planilha
                prontuariosAtualizados.add(prontuario);

                String nome = getCellValue(row.getCell(columnIndexMap.get("nome")));
                String mae = getCellValue(row.getCell(columnIndexMap.get("mãe")));
                String ultimaLocalizacao = getCellValue(row.getCell(columnIndexMap.get("última localização")));

                String unidade;
                String funcao;
                String tipoDeRegime;
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
                    // Para Layout 1, define "trabalha" como "sim" e "trabalhou" como o mês/ano
                    // atual.
                    trabalhou = now.format(formatter);
                }

                Optional<Malote> optionalCandidato = maloteRepository.findByProntuario(prontuario);
                Malote candidato;

                if (optionalCandidato.isPresent()) {
                    // Candidato já existe: atualizar ou manter campos conforme layout
                    candidato = optionalCandidato.get();
                    // Atualiza a data da atualização sempre
                    candidato.setDataDaAtualizacao(now);

                    if (isLayout2) {
                        // Layout2: atualiza os campos informados na planilha
                        candidato.setProntuario(prontuario);
                        candidato.setNome(nome);
                        candidato.setMae(mae);
                        candidato.setUnidade(unidade);
                        candidato.setUltimaLocalizacao(ultimaLocalizacao);
                        candidato.setTipoDeRegime(tipoDeRegime);
                        // Não altera biometria, trabalha, trabalhou e funcao
                    } else {
                        // Layout1: não altera os campos básicos se já existirem
                        // Campos que não devem ser modificados: prontuario, nome, mae, unidade,
                        // ultimaLocalizacao, tipoDeRegime, biometria
                        // Sempre atualiza o campo funcao com o valor da planilha
                        candidato.setFuncao(funcao);
                        // Define o campo trabalha como "sim"
                        candidato.setTrabalha("sim");
                        // Se for novo, o campo trabalhou será setado; para os já existentes, opta-se
                        // por não sobrescrever o valor anterior
                        // (a atualização de quem NÃO está na planilha será feita posteriormente)
                    }
                } else {
                    // Candidato não existe: cria um novo registro
                    candidato = new Malote();
                    candidato.setDataDaAtualizacao(now);

                    if (isLayout2) {
                        // Layout2: preenche os campos fornecidos pela planilha
                        candidato.setProntuario(prontuario);
                        candidato.setNome(nome);
                        candidato.setMae(mae);
                        candidato.setUnidade(unidade);
                        candidato.setUltimaLocalizacao(ultimaLocalizacao);
                        candidato.setTipoDeRegime(tipoDeRegime);
                        // Os campos que a planilha não fornece são criados como null
                        candidato.setBiometria(null);
                        candidato.setFuncao(null);
                        candidato.setTrabalha(null);
                        candidato.setTrabalhou(null);
                    } else {
                        // Layout1: preenche com os valores da planilha para os campos básicos
                        candidato.setProntuario(prontuario);
                        candidato.setNome(nome);
                        candidato.setMae(mae);
                        candidato.setUnidade(unidade);
                        candidato.setUltimaLocalizacao(ultimaLocalizacao);
                        candidato.setTipoDeRegime(tipoDeRegime);
                        // Biomatria é criado como null, pois a planilha não fornece esse dado
                        candidato.setBiometria(null);
                        // Sempre preenche o campo funcao com o valor da planilha
                        candidato.setFuncao(funcao);
                        // Define trabalha = "sim" e trabalhou com o valor atual
                        candidato.setTrabalha("sim");
                        candidato.setTrabalhou(trabalhou);
                    }
                }

                maloteRepository.save(candidato);
            }

            if (!isLayout2) {
                // Agora, para os candidatos que NÃO foram atualizados via planilha, atualiza o
                // campo "trabalha" para "nao".
                // Se o candidato já tinha "sim", adiciona uma vírgula e o valor do mês/ano
                // atual em "trabalhou".
                List<Malote> todosMalote = maloteRepository.findAll();
                for (Malote cand : todosMalote) {
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
                        maloteRepository.save(cand);
                    }
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

    @GetMapping("/oldest-data")
    public ResponseEntity<?> getOldestDataAtualizacao() {
        Optional<Malote> candidateOpt = maloteRepository.findTopByOrderByDataDaAtualizacaoAsc();
        if (candidateOpt.isPresent()) {
            LocalDateTime oldestData = candidateOpt.get().getDataDaAtualizacao();
            // Retorna em formato JSON, ex: { "oldestData": "2025-02-18T14:15:53.841" }
            return ResponseEntity.ok(Collections.singletonMap("oldestData", oldestData));
        }
        return ResponseEntity.ok(Collections.singletonMap("oldestData", null));
    }

}

class AlaPageNumberHelper extends PdfPageEventHelper {

    private final Map<Integer, String> pageToAla = new HashMap<>();
    private final Map<String, Integer> alaCounters = new HashMap<>();

    public void registerAlaForPage(int pageNumber, String ala) {
        pageToAla.put(pageNumber, ala);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        int currentPage = writer.getPageNumber();
        String ala = pageToAla.get(currentPage);

        if (ala == null)
            return;

        int numero = alaCounters.getOrDefault(ala, 1);
        alaCounters.put(ala, numero + 1);

        try {
            PdfContentByte cb = writer.getDirectContent();
            BaseFont baseFont = BaseFont.createFont("src/main/resources/fonts/Calibri-regular.ttf", BaseFont.CP1252,
                    BaseFont.EMBEDDED);
            cb.beginText();
            cb.setFontAndSize(baseFont, 12);
            cb.setColorFill(BaseColor.DARK_GRAY);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, String.valueOf(numero),
                    document.right() - 10, document.bottom() + 10, 0);
            cb.endText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
