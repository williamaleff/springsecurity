package com.william.springsecurity.repositories.prioridade;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.prioridade.Prioridade;

 
@Repository
public interface PrioridadeRepository extends JpaRepository<Prioridade, Long> {

    @Query(value = "select u from Prioridade u where u.nome like %?1%")
    List<Prioridade> findByNameContaining(String name);
}

