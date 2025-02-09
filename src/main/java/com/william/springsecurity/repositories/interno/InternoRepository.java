package com.william.springsecurity.repositories.interno;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.interno.Interno;

@Repository
public interface InternoRepository extends JpaRepository<Interno, Long> {

    @Query(value = "select u from Interno u where u.nome like %?1%")
    List<Interno> findByNameContaining(String name);

}
