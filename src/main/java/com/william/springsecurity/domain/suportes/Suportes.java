package com.william.springsecurity.domain.suportes;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@SequenceGenerator(name = "seq_suporte", sequenceName = "seq_suporte", allocationSize = 1, initialValue = 1)
public class Suportes implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_suporte")
	private Long id;
	
	private String tipoId;

	private String descricao;
	
	private String pessoaId;
	
	private String horario;
	
	private String data;
	
	private String chamadoGlpi;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTipoId() {
		return tipoId;
	}

	public void setTipoId(String tipoId) {
		this.tipoId = tipoId;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getPessoaId() {
		return pessoaId;
	}

	public void setPessoaId(String pessoaId) {
		this.pessoaId = pessoaId;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getChamadoGlpi() {
		return chamadoGlpi;
	}

	public void setChamadoGlpi(String chamadoGlpi) {
		this.chamadoGlpi = chamadoGlpi;
	}

}
