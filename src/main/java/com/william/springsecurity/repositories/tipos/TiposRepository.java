package com.william.springsecurity.repositories.tipos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.tipos.Tipos;

@Repository
public interface TiposRepository extends JpaRepository<Tipos, Long> {

  	@Query(value = "select u from Tipos u where u.nome like %?1%")
	List<Tipos> findByNameContaining(String name);
  	}
