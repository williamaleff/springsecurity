package com.william.springsecurity.repositories;

import com.william.springsecurity.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}