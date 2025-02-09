package com.william.springsecurity.domain.interno;
import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_interno", sequenceName = "seq_interno", allocationSize = 1, initialValue = 1)
public class Interno implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_interno")
    private Long id;
    private String nome;
    private String mae;
    private String localizacao;
    private String regime;
    private String funcao;
    private String prontuario;
    private String unidade;
    private String digital;
    private String foto;
    
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
    public String getMae() {
        return mae;
    }
    public void setMae(String mae) {
        this.mae = mae;
    }
    public String getLocalizacao() {
        return localizacao;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }
    public String getRegime() {
        return regime;
    }
    public void setRegime(String regime) {
        this.regime = regime;
    }
    public String getFuncao() {
        return funcao;
    }
    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }
    public String getProntuario() {
        return prontuario;
    }
    public void setProntuario(String prontuario) {
        this.prontuario = prontuario;
    }
    public String getUnidade() {
        return unidade;
    }
    public void setUnidade(String unidade) {
        this.unidade = unidade;
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
