package com.william.springsecurity.repositories.candidatos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.william.springsecurity.domain.candidatos.Candidatos;

import java.util.Optional;

public interface CandidatosRepository extends JpaRepository<Candidatos, Long> {
    Optional<Candidatos> findByProntuario(String prontuario);
}
