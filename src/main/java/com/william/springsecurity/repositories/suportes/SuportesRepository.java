package com.william.springsecurity.repositories.suportes;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.suportes.Suportes;

@Repository
public interface SuportesRepository extends JpaRepository<Suportes, Long> {

  	@Query(value = "select u from Suportes u where u.descricao like %?1%")
	List<Suportes> findByDescricaoContaining(String descricao);
  	
  	@Query(value = "select u from Suportes u where u.data like %?1%")
	List<Suportes> findByDateContaining(String date);
  	
  	@Query(value = "select u from Suportes u where u.data like %?1% AND u.descricao like %?2%") 
	List<Suportes> findByDateAndDescricaoContaining(String date, String descricao);
  	}
