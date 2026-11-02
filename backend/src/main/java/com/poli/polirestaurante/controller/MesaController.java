package com.poli.polirestaurante.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.dto.MesaResponse;
import com.poli.polirestaurante.service.AdministracionService;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final AdministracionService administracionService;

    public MesaController(AdministracionService administracionService) {
        this.administracionService = administracionService;
    }

    @GetMapping("/disponibles")
    public List<MesaResponse> listarDisponibles() {
        return administracionService.listarMesasDisponibles();
    }
}
