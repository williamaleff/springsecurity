package com.william.springsecurity.repositories.historico;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.historico.Historico;

 
@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Query(value = "select u from Historico u where u.chamadoId like %?1%")
    List<Historico> findByNameContaining(String name);
}

