package com.william.springsecurity.repositories.comentario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.comentario.Comentario;


@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @Query(value = "select u from Comentario u where u.chamadoId like %?1%")
    List<Comentario> findByNameContaining(String name);
}
