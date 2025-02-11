package com.william.springsecurity.controllers.biometria;

import com.nitgen.SDK.BSP.NBioBSPJNI;
import com.william.springsecurity.domain.biometria.dto.FingerprintRequest;
import com.william.springsecurity.domain.biometria.dto.FingerprintResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
@RequestMapping("/api")
public class BiometricController {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/verifyFingerprint")
    public ResponseEntity<FingerprintResponse> verifyFingerprint(@RequestBody FingerprintRequest request) {
        FingerprintResponse response = new FingerprintResponse();
        NBioBSPJNI bsp = new NBioBSPJNI();

        // Verifica se o SDK foi inicializado sem erros
        if (bsp.IsErrorOccured()) {
            response.setFound(false);
            response.setMessage("Erro ao iniciar o SDK: " + bsp.GetErrorCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        try {
            // Converter a string recebida em um objeto FIR_TEXTENCODE
            NBioBSPJNI.FIR_TEXTENCODE inputTextFIR = bsp.new FIR_TEXTENCODE();
            inputTextFIR.TextFIR = request.getFingerprint();

            // Cria um INPUT_FIR para o template recebido
            NBioBSPJNI.INPUT_FIR capturedInputFIR = bsp.new INPUT_FIR();
            capturedInputFIR.SetTextFIR(inputTextFIR);

            // Consulta os templates armazenados no banco de dados
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT id, nome, digital FROM interno")) {

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int userId = rs.getInt("id");
                    String nome = rs.getString("nome");
                    String biometria = rs.getString("digital");

                    // Converter o template armazenado em um INPUT_FIR
                    NBioBSPJNI.FIR_TEXTENCODE dbTextFIR = bsp.new FIR_TEXTENCODE();
                    dbTextFIR.TextFIR = biometria;

                    NBioBSPJNI.INPUT_FIR dbInputFIR = bsp.new INPUT_FIR();
                    dbInputFIR.SetTextFIR(dbTextFIR);

                   // Criar um array booleano mutável para armazenar o resultado
                   Boolean bResult = new Boolean(false);

                   // Comparar a digital capturada com a armazenada no banco
                   bsp.VerifyMatch(capturedInputFIR, dbInputFIR, bResult, null);

                    // Verifica se a digital foi encontrada
                        if (bResult) {
                            response.setFound(true);
                            response.setId(userId);
                            response.setNome(nome);
                            response.setMessage("Digital encontrada");
                        return ResponseEntity.ok(response);
                        }
                }
            } catch (SQLException ex) {
                response.setFound(false);
                response.setMessage("Erro no acesso ao banco de dados: " + ex.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Se nenhuma correspondência for encontrada
            response.setFound(false);
            response.setMessage("Digital não cadastrada");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setFound(false);
            response.setMessage("Erro: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}