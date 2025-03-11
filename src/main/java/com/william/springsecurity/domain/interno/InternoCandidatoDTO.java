package com.william.springsecurity.domain.interno;

public class InternoCandidatoDTO {
    // Dados do Interno
    private Long id;
    private String nome;
    private String prontuario;
    private String digital;
    private String foto;

    // Dados do Candidatos (para os campos que não existem em Interno)
    private String funcao;
    private String localizacao;
    private String mae;
    private String regime;
    private String unidade;

    // Getters e Setters

    public String getFuncao() {
        return funcao;
    }
    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
    public String getLocalizacao() {
        return localizacao;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
    public String getMae() {
        return mae;
    }
    public void setMae(String mae) {
        this.mae = mae;
    }
    public String getRegime() {
        return regime;
    }
    public void setRegime(String regime) {
        this.regime = regime;
    }
    public String getUnidade() {
        return unidade;
    }
    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getProntuario() {
        return prontuario;
    }
    public void setProntuario(String prontuario) {
        this.prontuario = prontuario;
    }
    
    public String getDigital() {
        return digital;
    }
    public void setDigital(String digital) {
        this.digital = digital;
    }
    public String getFoto() {
        return foto;
    }
    public void setFoto(String foto) {
        this.foto = foto;
    }
    
}
