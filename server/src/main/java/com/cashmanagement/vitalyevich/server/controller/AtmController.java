package com.cashmanagement.vitalyevich.server.controller;

import com.cashmanagement.vitalyevich.server.model.Atm;
import com.cashmanagement.vitalyevich.server.repository.AtmRepository;
import com.cashmanagement.vitalyevich.server.repository.CassetteRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AtmController {

    private final AtmRepository atmRepository;
    private final CassetteRepository cassetteRepository;

    public AtmController(AtmRepository atmRepository, CassetteRepository cassetteRepository) {
        this.atmRepository = atmRepository;
        this.cassetteRepository = cassetteRepository;
    }

    @QueryMapping
    Iterable<Atm> atms () {
        return atmRepository.findAll();
    }

}
