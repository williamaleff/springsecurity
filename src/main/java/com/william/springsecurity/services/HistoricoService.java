package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.historico.HistoricoRepository;


@Service
public class HistoricoService {

    @Autowired
    private HistoricoRepository historicoRepository;

    public long getTotalCount() {
        return historicoRepository.count();
    }

}
