package com.william.springsecurity.domain.tipos;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_tipos", sequenceName = "seq_tipos", allocationSize = 1, initialValue = 1)
public class Tipos implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_tipos")
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