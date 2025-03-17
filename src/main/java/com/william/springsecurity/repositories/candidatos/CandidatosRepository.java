package com.william.springsecurity.repositories.candidatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.william.springsecurity.domain.candidatos.Candidatos;

import java.util.List;
import java.util.Optional;

public interface CandidatosRepository extends JpaRepository<Candidatos, Long> {

    Optional<Candidatos> findByProntuario(String prontuario);

    Optional<Candidatos> findTopByOrderByDataDaAtualizacaoAsc();

    boolean existsByProntuarioAndTrabalhaIgnoreCase(String prontuario, String trabalha);

    Optional<Candidatos> findByProntuarioAndTrabalhaIgnoreCase(String prontuario, String trabalha);

    // Método para buscar candidatos com o campo 'trabalha' igual a "sim", ignorando
    // diferenças de case
    List<Candidatos> findByTrabalhaIgnoreCase(String trabalha);

    // Novo método para buscar candidatos que tenham o campo 'trabalhou' preenchido
    // (não nulo)
    List<Candidatos> findByTrabalhouIsNotNull();

    // Conta candidatos com trabalha = 'sim'
    long countByTrabalha(String trabalha);

    // Conta candidatos com trabalha = 'sim' e biometria = 'sim'
    long countByTrabalhaAndBiometria(String trabalha, String biometria);

    // Consulta para agrupar os valores de funcao e contar quantas vezes cada um
    // aparece (ignorando nulos)
    @Query("SELECT c.funcao, COUNT(c) FROM Candidatos c WHERE c.funcao IS NOT NULL GROUP BY c.funcao")
    List<Object[]> countFuncaoGroupByFuncao();

}
