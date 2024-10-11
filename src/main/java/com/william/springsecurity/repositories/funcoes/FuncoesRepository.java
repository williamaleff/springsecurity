package com.william.springsecurity.repositories.funcoes;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.funcoes.Funcoes;

 
@Repository
public interface FuncoesRepository extends JpaRepository<Funcoes, Long> {

    @Query(value = "select u from Funcoes u where u.nome like %?1%")
    List<Funcoes> findByNameContaining(String name);
}

