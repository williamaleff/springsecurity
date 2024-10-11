package com.william.springsecurity.repositories.agente;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.agente.Agente;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, Long> {

    @Query(value = "select u from Agente u where u.nome like %?1%")
    List<Agente> findByNameContaining(String name);
}

