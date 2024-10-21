package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.tipos.TiposRepository;


@Service
public class TiposService {

    @Autowired
    private TiposRepository tiposRepository;

    public long getTotalCount() {
        return tiposRepository.count();
    }

}
