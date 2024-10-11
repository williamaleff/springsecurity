package com.william.springsecurity.domain.chamado;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_chamado", sequenceName = "seq_chamado", allocationSize = 1, initialValue = 1)
public class Chamado implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chamado")
	
    private Long id;
	
	private String descricao;
    private String chamadoGlpi;
    private String dataCriacao;
    private String tipoId;
    private String clienteId;
    private String statusId;
    private String prioridadeId;
    private String dataAtualizacao;
    private String dataFechamento;
    private String agenteId;
    private String anexo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getChamadoGlpi() {
        return chamadoGlpi;
    }

    public void setChamadoGlpi(String chamadoGlpi) {
        this.chamadoGlpi = chamadoGlpi;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getTipoId() {
        return tipoId;
    }

    public void setTipoId(String tipoId) {
        this.tipoId = tipoId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getPrioridadeId() {
        return prioridadeId;
    }

    public void setPrioridadeId(String prioridadeId) {
        this.prioridadeId = prioridadeId;
    }

    public String getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(String dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(String dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public String getAgenteId() {
        return agenteId;
    }

    public void setAgenteId(String agenteId) {
        this.agenteId = agenteId;
    }

    public String getAnexo() {
        return anexo;
    }

    public void setAnexo(String anexo) {
        this.anexo = anexo;
    }
	
}