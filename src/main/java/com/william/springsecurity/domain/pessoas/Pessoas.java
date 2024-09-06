package com.william.springsecurity.domain.pessoas;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_pessoas", sequenceName = "seq_pessoas", allocationSize = 1, initialValue = 1)
public class Pessoas implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_pessoas")
	private Long id;
	
	private String nomeCompleto;
	
	private String email;
	
	private String funcaoId;
	
	private String informacoes;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFuncaoId() {
		return funcaoId;
	}

	public void setFuncaoId(String funcaoId) {
		this.funcaoId = funcaoId;
	}

	public String getInformacoes() {
		return informacoes;
	}

	public void setInformacoes(String informacoes) {
		this.informacoes = informacoes;
	}
		
}