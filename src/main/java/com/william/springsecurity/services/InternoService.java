package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.interno.InternoRepository;


@Service
public class InternoService {
    
    @Autowired
    private InternoRepository internoRepository;

    public long getTotalCount() {
        return internoRepository.count();
    }

}
