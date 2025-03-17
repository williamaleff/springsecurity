package com.william.springsecurity.domain.pontopdf;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class RegistroPontoPdf {
    // Representa o dia do mês (ex: 1, 2, 3, …)
    private int dayOfMonth;

    // Enum do java.time representando o dia da semana
    private DayOfWeek dayOfWeek;

    // Horários dos registros de ponto
    private LocalTime entrada;
    private LocalTime saidaAlmoco;
    private LocalTime retornoAlmoco;
    private LocalTime saida;

    // Indicador se o dia é final de semana (para aplicar formatação diferenciada)
    private boolean isWeekend;

    // Observação que será exibida para cada registro
    private String observacao;

    // Campos utilizados para ordenação (caso necessário)
    private String localizacao;
    private String nome;

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public boolean isWeekend() {
        return isWeekend;
    }

    public void setisWeekend(boolean isWeekend) {
        this.isWeekend = isWeekend;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
