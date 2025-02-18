package com.william.springsecurity.domain.user;

public record LoginResponseDTO(String token, String username, UserRole role) {
    // Construtor simplificado para casos antigos
    public LoginResponseDTO(String token) {
        this(token, null, null);
    }
}