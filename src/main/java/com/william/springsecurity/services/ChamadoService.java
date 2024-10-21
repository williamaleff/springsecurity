package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.chamado.ChamadoRepository;


@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    public long getTotalCount() {
        return chamadoRepository.count();
    }

}
