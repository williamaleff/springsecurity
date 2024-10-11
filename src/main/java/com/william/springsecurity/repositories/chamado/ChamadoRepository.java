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
}

