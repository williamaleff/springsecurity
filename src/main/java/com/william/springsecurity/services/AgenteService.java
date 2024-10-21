package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.agente.AgenteRepository;


@Service
public class AgenteService {

    @Autowired
    private AgenteRepository agenteRepository;

    public long getTotalCount() {
        return agenteRepository.count();
    }

}
