package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.funcoes.FuncoesRepository;


@Service
public class FuncoesService {

    @Autowired
    private FuncoesRepository funcoesRepository;

    public long getTotalCount() {
        return funcoesRepository.count();
    }

}
