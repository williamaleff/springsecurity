package com.william.springsecurity.controllers.ponto;

import java.time.LocalTime;

import com.william.springsecurity.domain.ponto.RegistroPonto;

public class RegistroPontoResponseDTO {

    private int quantidade;       // Quantidade de registros efetuados no dia
    private LocalTime entrada;
    private LocalTime saidaAlmoco;
    private LocalTime retornoAlmoco;
    private LocalTime saida;

    // Construtor padrão
    public RegistroPontoResponseDTO() {}

    // Getters e Setters
    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
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

    // Método estático para construir o DTO a partir de um RegistroPonto
    public static RegistroPontoResponseDTO from(RegistroPonto registro) {
        RegistroPontoResponseDTO dto = new RegistroPontoResponseDTO();

        int count = 0;
        if (registro.getEntrada() != null) count++;
        if (registro.getSaidaAlmoco() != null) count++;
        if (registro.getRetornoAlmoco() != null) count++;
        if (registro.getSaida() != null) count++;

        dto.setQuantidade(count);
        dto.setEntrada(registro.getEntrada());
        dto.setSaidaAlmoco(registro.getSaidaAlmoco());
        dto.setRetornoAlmoco(registro.getRetornoAlmoco());
        dto.setSaida(registro.getSaida());

        return dto;
    }
}
