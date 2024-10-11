package com.william.springsecurity.domain.comentario;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_comentario", sequenceName = "seq_comentario", allocationSize = 1, initialValue = 1)
public class Comentario implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_comentario")
	private Long id;
	
	private String chamadoId;
    private String agenteId;
    private String clienteId;
    private String dataCriacao;
    private String comentario;

    public String getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(String chamadoId) {
        this.chamadoId = chamadoId;
    }

    public String getAgenteId() {
        return agenteId;
    }

    public void setAgenteId(String agenteId) {
        this.agenteId = agenteId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

 	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}