package com.william.springsecurity.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.william.springsecurity.repositories.status.StatusRepository;


@Service
public class StatusService {

    @Autowired
    private StatusRepository statusRepository;

    public long getTotalCount() {
        return statusRepository.count();
    }

}
