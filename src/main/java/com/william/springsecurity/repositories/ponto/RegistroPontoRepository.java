package com.william.springsecurity.repositories.ponto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.william.springsecurity.domain.ponto.RegistroPonto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, Long> {
    Optional<RegistroPonto> findByFuncionarioIdAndDia(Long funcionarioId, LocalDate dia);

    // Novo método para buscar registros entre duas datas
    List<RegistroPonto> findByDiaBetween(LocalDate inicio, LocalDate fim);

    // Novo método para filtrar por funcionário e intervalo de datas
    List<RegistroPonto> findByFuncionarioIdAndDiaBetween(Long funcionarioId, LocalDate inicio, LocalDate fim);

    @Query("SELECT rp FROM RegistroPonto rp JOIN FETCH rp.interno WHERE rp.dia BETWEEN :inicio AND :fim")
    List<RegistroPonto> findByDiaBetweenWithInterno(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    List<RegistroPonto> findByFuncionarioIdInAndDiaBetween(List<Long> funcionarioIds, LocalDate inicio, LocalDate fim);

}
