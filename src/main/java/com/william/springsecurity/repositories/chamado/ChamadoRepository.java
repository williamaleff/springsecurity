package com.william.springsecurity.repositories.chamado;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.chamado.Chamado;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    @Query(value = "select u from Chamado u where u.descricao like %?1%")
    List<Chamado> findByNameContaining(String name);

    @Query(value = "select u from Chamado u where u.dataCriacao BETWEEN ?1 AND ?2") 
	List<Chamado> findByPeriod(String dataInicial, String dataFinal);

    @Query(value = "select u from Chamado u where u.dataCriacao like %?1%")
	List<Chamado> findByDateContaining(String date);
  	
  	@Query(value = "select u from Chamado u where u.dataCriacao like %?1% AND u.descricao like %?2%") 
	List<Chamado> findByDateAndDescricaoContaining(String date, String descricao);
  	
}

