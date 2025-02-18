package com.william.springsecurity.repositories;

import com.william.springsecurity.domain.user.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, String> {
    UserDetails findByLogin(String login);

    @Query(value = "select u from users u where u.login like %?1%")
    List<User> findByNameContaining(String name);
}