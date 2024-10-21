package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.cliente.ClienteRepository;


@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public long getTotalCount() {
        return clienteRepository.count();
    }

}
