package com.william.springsecurity.domain.pontopdf;

import java.util.List;

public class RelatorioPontoDTO {
    
    private int ano;
    private int mes;
    
    // Nome do arquivo da imagem (por exemplo, logo da instituição)
    private String imagem;
    
    // Dados de cabeçalho
    private String cabecalho1;
    private String cabecalho2;
    private String cabecalho3;
    private String cabecalho4;
    
    // Título do relatório
    private String titulo;
    
    // Dados do funcionário
    private String nome;
    private String funcao;
    private String ultimaLocalizacao;
    private String prontuario;
    private String mae;

    // Lista dos registros de ponto do funcionário
    private List<RegistroPontoPdf> registrosPontos;
    
    // Unidade ou departamento, utilizado no rodapé
    private String unidade;


    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getCabecalho1() {
        return cabecalho1;
    }

    public void setCabecalho1(String cabecalho1) {
        this.cabecalho1 = cabecalho1;
    }

    public String getCabecalho2() {
        return cabecalho2;
    }

    public void setCabecalho2(String cabecalho2) {
        this.cabecalho2 = cabecalho2;
    }

    public String getCabecalho3() {
        return cabecalho3;
    }

    public void setCabecalho3(String cabecalho3) {
        this.cabecalho3 = cabecalho3;
    }

    public String getCabecalho4() {
        return cabecalho4;
    }

    public void setCabecalho4(String cabecalho4) {
        this.cabecalho4 = cabecalho4;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getUltimaLocalizacao() {
        return ultimaLocalizacao;
    }

    public void setUltimaLocalizacao(String ultimaLocalizacao) {
        this.ultimaLocalizacao = ultimaLocalizacao;
    }

    public String getProntuario() {
        return prontuario;
    }

    public void setProntuario(String prontuario) {
        this.prontuario = prontuario;
    }

    public String getMae() {
        return mae;
    }

    public void setMae(String mae) {
        this.mae = mae;
    }

    public List<RegistroPontoPdf> getRegistrosPontos() {
        return registrosPontos;
    }

    public void setRegistrosPontos(List<RegistroPontoPdf> registrosPontos) {
        this.registrosPontos = registrosPontos;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    
}