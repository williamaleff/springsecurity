package com.william.springsecurity.domain.status;

import java.io.Serializable;
import jakarta.persistence.*;
 
@Entity
@SequenceGenerator(name = "seq_status", sequenceName = "seq_status", allocationSize = 1, initialValue = 1)
public class Status implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_status")
	private Long id;
	
	private String nome;

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
		
}