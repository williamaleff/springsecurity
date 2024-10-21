package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.prioridade.PrioridadeRepository;


@Service
public class PrioridadeService {

    @Autowired
    private PrioridadeRepository prioridadeRepository;

    public long getTotalCount() {
        return prioridadeRepository.count();
    }

}
