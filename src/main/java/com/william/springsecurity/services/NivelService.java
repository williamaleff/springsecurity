package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.nivel.NivelRepository;


@Service
public class NivelService {

    @Autowired
    private NivelRepository nivelRepository;

    public long getTotalCount() {
        return nivelRepository.count();
    }

}
