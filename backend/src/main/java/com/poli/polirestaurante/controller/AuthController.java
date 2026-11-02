package com.poli.polirestaurante.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.dto.SesionResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public SesionResponse usuarioActual(Authentication authentication) {
        String rol = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La sesión no tiene un rol asignado."));
        return new SesionResponse(authentication.getName(), rol);
    }
}
