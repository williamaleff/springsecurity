package com.william.springsecurity.domain.user;

public record RegisterDTO(String login, String password, UserRole role) {
}