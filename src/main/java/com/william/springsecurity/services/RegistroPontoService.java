package com.william.springsecurity.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.repositories.ponto.RegistroPontoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.Duration;

@Service
public class RegistroPontoService {

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private InternoRepository internoRepository;

    public RegistroPonto registrarPonto(Long funcionarioId) {
        LocalDate hoje = LocalDate.now();
         Optional<RegistroPonto> registroOpt = registroPontoRepository.findByFuncionarioIdAndDia(funcionarioId, hoje);
    RegistroPonto registro = registroOpt.orElseGet(() -> {
        // Busca o Interno correspondente
        Interno interno = internoRepository.findById(funcionarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionário não encontrado"));
        
        // Cria o novo registro com o Interno associado
        RegistroPonto novoRegistro = new RegistroPonto(funcionarioId, hoje);
        novoRegistro.setInterno(interno);
        return novoRegistro;
    });

        // Verifica se todos os 4 horários já foram preenchidos.
        if (registro.getEntrada() != null &&
            registro.getSaidaAlmoco() != null &&
            registro.getRetornoAlmoco() != null &&
            registro.getSaida() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O funcionário já bateu o ponto 4 vezes hoje.");
        }

        // Registra o próximo horário disponível, seguindo a ordem:
        if (registro.getEntrada() == null) {
            registro.setEntrada(LocalTime.now());
        } else if (registro.getSaidaAlmoco() == null) {
            registro.setSaidaAlmoco(LocalTime.now());
        } else if (registro.getRetornoAlmoco() == null) {
            registro.setRetornoAlmoco(LocalTime.now());
        } else if (registro.getSaida() == null) {
            registro.setSaida(LocalTime.now());
        }

        return registroPontoRepository.save(registro);
    }

    // Novo método para listar registros agrupados por funcionarioId
    public Map<Long, List<RegistroPonto>> listarRegistrosAgrupadosPorFuncionario() {
        List<RegistroPonto> registros = registroPontoRepository.findAll();
        return registros.stream()
                .collect(Collectors.groupingBy(RegistroPonto::getFuncionarioId));
    }

    // Novo método para listar registros agrupados por funcionário para um determinado mês
    public Map<Long, List<RegistroPonto>> listarRegistrosAgrupadosPorFuncionarioMes(int ano, int mes) {
        // Define o primeiro dia do mês e o último dia do mês
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());
        
        // Busca os registros dentro do intervalo
        List<RegistroPonto> registros = registroPontoRepository.findByDiaBetween(inicio, fim);
        
        // Agrupa os registros pelo funcionário
        return registros.stream()
                .collect(Collectors.groupingBy(RegistroPonto::getFuncionarioId));
    }

    // Novo método para calcular as horas trabalhadas de um funcionário em um mês
    public String calcularHorasTrabalhadasFuncionarioMes(Long funcionarioId, int ano, int mes) {
        // Define o intervalo do mês
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());
        
        // Busca os registros do funcionário no período
        List<RegistroPonto> registros = registroPontoRepository.findByFuncionarioIdAndDiaBetween(funcionarioId, inicio, fim);
        
        // Soma as horas trabalhadas (considerando que o método getHorasTrabalhadas() retorna um objeto Duration ou null)
        Duration total = Duration.ZERO;
        for (RegistroPonto registro : registros) {
            Duration duracao = registro.getHorasTrabalhadas();
            if (duracao != null) {
                total = total.plus(duracao);
            }
        }
        
        // Formata a duração em HH:mm:ss
        long hours = total.toHours();
        long minutes = total.minusHours(hours).toMinutes();
        long seconds = total.minusHours(hours).minusMinutes(minutes).getSeconds();
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public List<RegistroPonto> listarRegistrosPorFuncionarioMes(Long funcionarioId, int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());
        return registroPontoRepository.findByFuncionarioIdAndDiaBetween(funcionarioId, inicio, fim);
    }
    
}
