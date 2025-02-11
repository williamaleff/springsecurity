package com.william.springsecurity.domain.biometria.dto;

public class FingerprintResponse {
    private boolean found;
    private String message;
    private Integer id;    // Pode ser nulo se não encontrar o usuário
    private String nome;   // Pode ser nulo se não encontrar o usuário

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}