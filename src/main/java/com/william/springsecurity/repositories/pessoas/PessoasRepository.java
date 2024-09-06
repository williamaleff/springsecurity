package com.william.springsecurity.repositories.pessoas;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.pessoas.Pessoas;


@Repository
public interface PessoasRepository extends JpaRepository<Pessoas, Long> {

  	@Query(value = "select u from Pessoas u where u.nomeCompleto like %?1%")
	List<Pessoas> findByNameContaining(String name);
}
