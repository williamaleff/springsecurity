package com.william.springsecurity.repositories.cliente;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.cliente.Cliente;


@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query(value = "select u from Cliente u where u.nome like %?1%")
    List<Cliente> findByNameContaining(String name);
}

