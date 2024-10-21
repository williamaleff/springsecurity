package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.comentario.ComentarioRepository;


@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    public long getTotalCount() {
        return comentarioRepository.count();
    }

}
