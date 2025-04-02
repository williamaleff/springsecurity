package com.william.springsecurity.domain.interno;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_interno", sequenceName = "seq_interno", allocationSize = 1, initialValue = 1)
public class Interno implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_interno")
    private Long id;
    private String nome;
    private String prontuario;

    @Column(columnDefinition = "TEXT")
    private String digital;
    
    private String foto;
    private LocalDateTime dataDaAtualizacao;
    
    public LocalDateTime getDataDaAtualizacao() {
        return dataDaAtualizacao;
    }
    public void setDataDaAtualizacao(LocalDateTime dataDaAtualizacao) {
        this.dataDaAtualizacao = dataDaAtualizacao;
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
