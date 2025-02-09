package com.william.springsecurity.repositories.ponto;

import org.springframework.data.jpa.repository.JpaRepository;

import com.william.springsecurity.domain.ponto.RegistroPonto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {
    Optional<RegistroPonto> findByFuncionarioIdAndDia(Long funcionarioId, LocalDate dia);

    // Novo método para buscar registros entre duas datas
    List<RegistroPonto> findByDiaBetween(LocalDate inicio, LocalDate fim);

}
