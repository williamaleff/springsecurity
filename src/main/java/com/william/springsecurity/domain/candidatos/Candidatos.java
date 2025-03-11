package com.william.springsecurity.domain.candidatos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Candidatos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String prontuario;
    private String nome;
    private String mae;
    private String unidade;
    private String ultimaLocalizacao;
    private String tipoDeRegime;
    private String funcao;
    private LocalDateTime dataDaAtualizacao;
    private String biometria;
    private String trabalha;
    private String trabalhou;
    
    public String getBiometria() {
        return biometria;
    }

    public void setBiometria(String biometria) {
        this.biometria = biometria;
    }

    public String getTrabalha() {
        return trabalha;
    }

    public void setTrabalha(String trabalha) {
        this.trabalha = trabalha;
    }

    public String getTrabalhou() {
        return trabalhou;
    }

    public void setTrabalhou(String trabalhou) {
        this.trabalhou = trabalhou;
    }

    public Candidatos() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getProntuario() {
        return prontuario;
    }
    public void setProntuario(String prontuario) {
        this.prontuario = prontuario;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getMae() {
        return mae;
    }
    public void setMae(String mae) {
        this.mae = mae;
    }
    public String getUnidade() {
        return unidade;
    }
    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
    public String getUltimaLocalizacao() {
        return ultimaLocalizacao;
    }
    public void setUltimaLocalizacao(String ultimaLocalizacao) {
        this.ultimaLocalizacao = ultimaLocalizacao;
    }
    public String getTipoDeRegime() {
        return tipoDeRegime;
    }
    public void setTipoDeRegime(String tipoDeRegime) {
        this.tipoDeRegime = tipoDeRegime;
    }
    public String getFuncao() {
        return funcao;
    }
    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
    public LocalDateTime getDataDaAtualizacao() {
        return dataDaAtualizacao;
    }
    public void setDataDaAtualizacao(LocalDateTime dataDaAtualizacao) {
        this.dataDaAtualizacao = dataDaAtualizacao;
    }
}
