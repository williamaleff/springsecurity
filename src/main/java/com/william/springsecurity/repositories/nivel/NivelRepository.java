package com.william.springsecurity.repositories.nivel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.nivel.Nivel;

 
@Repository
public interface NivelRepository extends JpaRepository<Nivel, Long> {

    @Query(value = "select u from Nivel u where u.nome like %?1%")
    List<Nivel> findByNameContaining(String name);
}

