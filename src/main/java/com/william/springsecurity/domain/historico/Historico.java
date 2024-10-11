package com.william.springsecurity.domain.historico;

import java.io.Serializable;
import jakarta.persistence.*;
 
@Entity
@SequenceGenerator(name = "seq_historico", sequenceName = "seq_historico", allocationSize = 1, initialValue = 1)
public class Historico implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_historico")
	private Long id;
	
	private String chamadoId;
    private String statusAnteriorId;
    private String statusNovoId;
    private String data;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public String getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(String chamadoId) {
        this.chamadoId = chamadoId;
    }

    public String getStatusAnteriorId() {
        return statusAnteriorId;
    }

    public void setStatusAnteriorId(String statusAnteriorId) {
        this.statusAnteriorId = statusAnteriorId;
    }

    public String getStatusNovoId() {
        return statusNovoId;
    }

    public void setStatusNovoId(String statusNovoId) {
        this.statusNovoId = statusNovoId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

	
    
}