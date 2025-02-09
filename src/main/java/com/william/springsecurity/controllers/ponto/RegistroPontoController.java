package com.william.springsecurity.controllers.ponto;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.services.RegistroPontoService;

@RestController
@RequestMapping("/ponto")
public class RegistroPontoController {

    @Autowired
    private RegistroPontoService registroPontoService;

    @PostMapping("/registrar/{funcionarioId}")
    public ResponseEntity<String> registrarPonto(@PathVariable Long funcionarioId) {
        registroPontoService.registrarPonto(funcionarioId);
        return ResponseEntity.ok("Ponto registrado com sucesso!");
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

}
