package com.william.springsecurity.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.repositories.ponto.RegistroPontoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.Duration;

@Service
public class RegistroPontoService {

    @Autowired
private RegistroPontoRepository registroPontoRepository;

@Autowired
private InternoRepository internoRepository;

public RegistroPonto registrarPonto(Long funcionarioId) {
    // Verifica se o funcionário (Interno) existe
    Interno interno = internoRepository.findById(funcionarioId)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado"));

    LocalDate hoje = LocalDate.now();
    LocalTime agora = LocalTime.now();

    // Buscar o último registro de ponto do funcionário no dia
    Optional<RegistroPonto> ultimoRegistroOpt = registroPontoRepository.findByFuncionarioIdAndDia(funcionarioId, hoje);
    if (ultimoRegistroOpt.isPresent()) {
        RegistroPonto ultimoRegistro = ultimoRegistroOpt.get();
        // Determinar o último horário registrado (entre os 4 possíveis)
        LocalTime ultimoHorario = Stream.of(
                ultimoRegistro.getEntrada(),
                ultimoRegistro.getSaidaAlmoco(),
                ultimoRegistro.getRetornoAlmoco(),
                ultimoRegistro.getSaida()
        ).filter(Objects::nonNull)
         .max(Comparator.naturalOrder())
         .orElse(null);

        // Se o último horário foi registrado há menos de 10 minutos, bloqueia o novo registro
        if (ultimoHorario != null && Duration.between(ultimoHorario, agora).toMinutes() < 10) {
            throw new IllegalArgumentException("Registro não permitido. Aguarde 10 minutos antes de registrar novamente.");
        }
    }
    
    // Cria ou atualiza o registro de ponto associando o Interno encontrado
    return salvarOuAtualizarRegistro(funcionarioId, interno, hoje, agora);
}

public RegistroPonto salvarOuAtualizarRegistro(Long funcionarioId, Interno interno, LocalDate data, LocalTime horario) {
    Optional<RegistroPonto> registroOpt = registroPontoRepository.findByFuncionarioIdAndDia(funcionarioId, data);
    RegistroPonto registro = null; // Inicializa a variável

    if (registroOpt.isPresent()) {
        registro = registroOpt.get();

        if (registro.getEntrada() == null) {
            registro.setEntrada(horario);
        } else if (registro.getSaidaAlmoco() == null) {
            registro.setSaidaAlmoco(horario);
        } else if (registro.getRetornoAlmoco() == null) {
            registro.setRetornoAlmoco(horario);
        } else if (registro.getSaida() == null) {
            registro.setSaida(horario);
        } else {
            throw new IllegalArgumentException("Todos os pontos já foram registrados para hoje.");
        }
    } else {
        registro = new RegistroPonto();
        registro.setFuncionarioId(funcionarioId);
        registro.setDia(data);
        registro.setDiaSemana(data.getDayOfWeek().toString());
        registro.setEntrada(horario); // Primeiro horário registrado do dia
        registro.setInterno(interno);
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

    public List<RegistroPonto> listarRegistrosPorFuncionarioMes(Long funcionarioId, int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());
        
        List<RegistroPonto> registros = registroPontoRepository.findByFuncionarioIdAndDiaBetween(funcionarioId, inicio, fim);
        
        // Atualiza o campo horasTrabalhadas de acordo com as regras:
        for (RegistroPonto registro : registros) {
            Duration horas = calcularHorasTrabalhadasCustom(registro);
            registro.setHorasTrabalhadas(horas);
        }
        
        return registros;
    }
    
    /**
     * Calcula as horas trabalhadas para um dia, conforme:
     * - Se houver 2 ou 3 registros: retorna a diferença entre o primeiro e o segundo registro.
     * - Se houver 4 registros: retorna (saidaAlmoco - entrada) + (saida - retornoAlmoco).
     */
    private Duration calcularHorasTrabalhadasCustom(RegistroPonto registro) {
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
    
    public String calcularHorasTrabalhadasFuncionarioMes(Long funcionarioId, int ano, int mes) {
    // Define o intervalo do mês
    LocalDate inicio = LocalDate.of(ano, mes, 1);
    LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());
    
    // Busca os registros do funcionário no período
    List<RegistroPonto> registros = registroPontoRepository.findByFuncionarioIdAndDiaBetween(funcionarioId, inicio, fim);
    
    // Soma as horas trabalhadas usando a lógica customizada para cada registro
    Duration total = Duration.ZERO;
    for (RegistroPonto registro : registros) {
        Duration duracao = calcularHorasTrabalhadas(registro);
        total = total.plus(duracao);
    }
    
    // Formata a duração em HH:mm:ss
    long hours = total.toHours();
    long minutes = total.minusHours(hours).toMinutes();
    long seconds = total.minusHours(hours).minusMinutes(minutes).getSeconds();
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
}

/**
 * Calcula as horas trabalhadas em um dia considerando:
 * - Se há 2 ou 3 registros: utiliza o intervalo do primeiro ao segundo registro.
 * - Se há 4 registros: soma o intervalo (primeiro ao segundo) e (terceiro ao quarto).
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
    
    // Se houver pelo menos 2 registros, use o intervalo do primeiro ao segundo
    if (pontos.size() >= 2) {
        if (pontos.size() == 2 || pontos.size() == 3) {
            return Duration.between(pontos.get(0), pontos.get(1));
        } else if (pontos.size() >= 4) {
            Duration periodoManha = Duration.between(pontos.get(0), pontos.get(1));
            Duration periodoTarde = Duration.between(pontos.get(2), pontos.get(3));
            return periodoManha.plus(periodoTarde);
        }
    }
    return Duration.ZERO;
}

    
}
