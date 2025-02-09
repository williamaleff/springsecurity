package com.william.springsecurity.domain.ponto;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "registro_ponto", uniqueConstraints = @UniqueConstraint(columnNames = {"funcionario_id", "dia"}))
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "funcionario_id", nullable = false)
    private Long funcionarioId;

    @Column(name = "dia", nullable = false)
    private LocalDate dia;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana;

    // Quatro horários de batida do ponto:
    @Column(name = "entrada")
    private LocalTime entrada;

    @Column(name = "saida_almoco")
    private LocalTime saidaAlmoco;

    @Column(name = "retorno_almoco")
    private LocalTime retornoAlmoco;

    @Column(name = "saida")
    private LocalTime saida;

    // Campo opcional para exibir o cálculo de horas trabalhadas (não persistido)
    @Transient
    private Duration horasTrabalhadas;

    @Column(name = "observacao")
    private String observacao;

    // Construtor padrão
    public RegistroPonto() {}

    // Construtor com funcionarioId e data, que também preenche o dia da semana
    public RegistroPonto(Long funcionarioId, LocalDate dia) {
        this.funcionarioId = funcionarioId;
        this.dia = dia;
        this.diaSemana = dia.getDayOfWeek().toString();
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public Long getFuncionarioId() {
        return funcionarioId;
    }

    public void setFuncionarioId(Long funcionarioId) {
        this.funcionarioId = funcionarioId;
    }

    public LocalDate getDia() {
        return dia;
    }

    public void setDia(LocalDate dia) {
        this.dia = dia;
        this.diaSemana = dia.getDayOfWeek().toString();
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getEntrada() {
        return entrada;
    }

    public void setEntrada(LocalTime entrada) {
        this.entrada = entrada;
    }

    public LocalTime getSaidaAlmoco() {
        return saidaAlmoco;
    }

    public void setSaidaAlmoco(LocalTime saidaAlmoco) {
        this.saidaAlmoco = saidaAlmoco;
    }

    public LocalTime getRetornoAlmoco() {
        return retornoAlmoco;
    }

    public void setRetornoAlmoco(LocalTime retornoAlmoco) {
        this.retornoAlmoco = retornoAlmoco;
    }

    public LocalTime getSaida() {
        return saida;
    }

    public void setSaida(LocalTime saida) {
        this.saida = saida;
    }

    // Cálculo das horas trabalhadas (caso os 4 horários estejam preenchidos)
    public Duration getHorasTrabalhadas() {
        if (entrada != null && saidaAlmoco != null && retornoAlmoco != null && saida != null) {
            Duration periodoManha = Duration.between(entrada, saidaAlmoco);
            Duration periodoTarde = Duration.between(retornoAlmoco, saida);
            return periodoManha.plus(periodoTarde);
        }
        return null;
    }

    public void setHorasTrabalhadas(Duration horasTrabalhadas) {
        this.horasTrabalhadas = horasTrabalhadas;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
