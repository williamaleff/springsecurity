package com.william.springsecurity.domain.ponto;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;


@Entity
@Table(name = "registro_ponto", uniqueConstraints = @UniqueConstraint(columnNames = {"funcionario_id", "dia"}))
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "funcionario_id", insertable = false, updatable = false)
    private Long funcionarioId;
    
    @Column(name = "dia", nullable = false)
    private LocalDate dia;

    // Quatro horários de batida do ponto:
    @Column(name = "entrada")
    private LocalTime entrada;

    @Column(name = "saida_almoco")
    private LocalTime saidaAlmoco;

    @Column(name = "retorno_almoco")
    private LocalTime retornoAlmoco;

    @Column(name = "saida")
    private LocalTime saida;

    @Column(name = "observacao")
    private String observacao;

    // Construtor padrão
    public RegistroPonto() {}

    // Construtor com funcionarioId e data, que também preenche o dia da semana
    public RegistroPonto(Long funcionarioId, LocalDate dia) {
        this.funcionarioId = funcionarioId;
        this.dia = dia;
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

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getDiaSemana() {
        return this.dia.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
    }
    
}
