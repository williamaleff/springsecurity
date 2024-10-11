package com.william.springsecurity.repositories.status;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.william.springsecurity.domain.status.Status;

 
@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    @Query(value = "select u from Status u where u.nome like %?1%")
    List<Status> findByNameContaining(String name);
}

