package com.william.springsecurity.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.domain.candidatos.Candidatos;
import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.ponto.RegistroPonto;
import com.william.springsecurity.domain.pontopdf.RegistroPontoPdf;
import com.william.springsecurity.domain.pontopdf.RelatorioPontoDTO;
import com.william.springsecurity.repositories.candidatos.CandidatosRepository;
import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.repositories.ponto.RegistroPontoRepository;

@Service
public class RelatorioPontoService {

    @Autowired
    private InternoRepository internoRepository;

    @Autowired
    private CandidatosRepository candidatosRepository;

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    public RelatorioPontoDTO createRelatorioPontoDTO(Long funcionarioId, int ano, int mes) {
        // Busca o Interno pelo id (funcionarioId)
        Interno interno = internoRepository.findById(funcionarioId)
            .orElseThrow(() -> new RuntimeException("Interno não encontrado com id: " + funcionarioId));

        // Obtém o prontuário do Interno
        String prontuario = interno.getProntuario();

        // Busca o candidato usando o prontuário obtido
        Candidatos candidato = candidatosRepository.findByProntuario(prontuario)
                .orElseThrow(() -> new RuntimeException("Candidato não encontrado com id: " + funcionarioId));

        // Cria e popula o DTO com dados fixos e do candidato
        RelatorioPontoDTO dto = new RelatorioPontoDTO();

        // Preenchimento manual dos campos fixos
        dto.setImagem("logo.png");
        dto.setCabecalho1("CEARÁ");
        dto.setCabecalho2("GOVERNO DO ESTADO");
        dto.setCabecalho3("SECRETARIA DA ADMINISTRAÇÃO");
        dto.setCabecalho4("PENITENCIÁRIA E RESSOCIALIZAÇÃO");
        dto.setCabecalho5("UNIDADE PRISIONAL REGIONAL DE SOBRAL");
        dto.setTitulo("FOLHA INDIVIDUAL DE FREQUÊNCIA  - REMIÇÃO");

        // Dados do candidato
        dto.setNome(candidato.getNome());
        dto.setFuncao(candidato.getFuncao());
        dto.setUltimaLocalizacao(candidato.getUltimaLocalizacao());
        dto.setProntuario(candidato.getProntuario());
        dto.setMae(candidato.getMae());
        dto.setUnidade(candidato.getUnidade());
        dto.setAno(ano);
        dto.setMes(mes);

        // Define o intervalo de datas para o mês/ano informado
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.with(TemporalAdjusters.lastDayOfMonth());

        // Busca os registros de ponto para o funcionário no período
        List<RegistroPonto> registros = registroPontoRepository
                .findByFuncionarioIdAndDiaBetween(funcionarioId, inicio, fim);

        // Organiza os registros em um Map para facilitar a busca por data
        Map<LocalDate, RegistroPonto> registrosMap = registros.stream()
                .collect(Collectors.toMap(RegistroPonto::getDia, rp -> rp));

        // Cria uma lista de RegistroPontoPdf para todos os dias do mês
        List<RegistroPontoPdf> registrosPontos = new ArrayList<>();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            RegistroPontoPdf rpdf = new RegistroPontoPdf();
            rpdf.setDayOfMonth(data.getDayOfMonth());
            rpdf.setDayOfWeek(data.getDayOfWeek());
            // isWeekend é true se o dia for Sábado ou Domingo
            rpdf.setisWeekend(data.getDayOfWeek() == DayOfWeek.SATURDAY || data.getDayOfWeek() == DayOfWeek.SUNDAY);

            // Preenche os campos 'nome' e 'localizacao' com os valores da tabela candidatos
            rpdf.setNome(candidato.getNome());
            rpdf.setLocalizacao(candidato.getUltimaLocalizacao());

            // Se houver um registro para o dia atual, preenche os demais campos
            RegistroPonto rp = registrosMap.get(data);
            if (rp != null) {
                rpdf.setEntrada(rp.getEntrada());
                rpdf.setSaidaAlmoco(rp.getSaidaAlmoco());
                rpdf.setRetornoAlmoco(rp.getRetornoAlmoco());
                rpdf.setSaida(rp.getSaida());
                rpdf.setObservacao(rp.getObservacao());
            }
            registrosPontos.add(rpdf);
        }
        dto.setRegistrosPontos(registrosPontos);

        return dto;
    }
}
