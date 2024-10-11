package com.william.springsecurity.domain.agente;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_agente", sequenceName = "seq_agente", allocationSize = 1, initialValue = 1)
public class Agente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_agente")
    private Long id;

    private String nome;

    private String email;

    private String dataContratacao;

    private String funcaoId;

    private String telefone;

    private String dataCriacao;

    private String nivelId;

    private String outros;

    private String foto;

    private String anexo;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDataContratacao() {
        return dataContratacao;
    }

    public void setDataContratacao(String dataContratacao) {
        this.dataContratacao = dataContratacao;
    }

    public String getFuncaoId() {
        return funcaoId;
    }

    public void setFuncaoId(String funcaoId) {
        this.funcaoId = funcaoId;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getNivelId() {
        return nivelId;
    }

    public void setNivelId(String nivelId) {
        this.nivelId = nivelId;
    }

    public String getOutros() {
        return outros;
    }

    public void setOutros(String outros) {
        this.outros = outros;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getAnexo() {
        return anexo;
    }

    public void setAnexo(String anexo) {
        this.anexo = anexo;
    }

}