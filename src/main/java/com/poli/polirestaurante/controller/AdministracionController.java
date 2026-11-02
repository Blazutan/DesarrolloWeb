package com.poli.polirestaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.dto.MesaResponse;
import com.poli.polirestaurante.dto.MesaRequest;
import com.poli.polirestaurante.dto.UsuarioRequest;
import com.poli.polirestaurante.dto.UsuarioResponse;
import com.poli.polirestaurante.service.AdministracionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/administracion")
public class AdministracionController {

    private final AdministracionService administracionService;

    public AdministracionController(AdministracionService administracionService) {
        this.administracionService = administracionService;
    }

    @GetMapping("/mesas")
    public List<MesaResponse> listarMesas() {
        return administracionService.listarMesas();
    }

    @GetMapping("/mesas/{id}")
    public ResponseEntity<MesaResponse> buscarMesa(@PathVariable Long id) {
        MesaResponse mesa = administracionService.buscarMesa(id);
        return mesa == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(mesa);
    }

    @PostMapping("/mesas")
    public ResponseEntity<?> crearMesa(@Valid @RequestBody MesaRequest datos) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(administracionService.crearMesa(datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/mesas/{id}")
    public ResponseEntity<?> actualizarMesa(@PathVariable Long id, @Valid @RequestBody MesaRequest datos) {
        try {
            MesaResponse mesa = administracionService.actualizarMesa(id, datos);
            return mesa == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/mesas/{id}")
    public ResponseEntity<Void> eliminarMesa(@PathVariable Long id) {
        return administracionService.eliminarMesa(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PatchMapping("/mesas/{id}/estado")
    public ResponseEntity<?> actualizarEstadoMesa(@PathVariable Long id, @RequestParam String estado) {
        try {
            MesaResponse mesa = administracionService.actualizarEstadoMesa(id, estado);
            return mesa == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(mesa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listarUsuarios() {
        return administracionService.listarUsuarios();
    }

    @GetMapping("/usuarios/{rol}/{id}")
    public ResponseEntity<UsuarioResponse> buscarUsuario(@PathVariable String rol, @PathVariable Long id) {
        try {
            UsuarioResponse usuario = administracionService.buscarUsuario(rol, id);
            return usuario == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/usuarios/{rol}")
    public ResponseEntity<?> crearUsuario(@PathVariable String rol, @Valid @RequestBody UsuarioRequest datos) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(administracionService.crearUsuario(rol, datos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/usuarios/{rol}/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable String rol, @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest datos) {
        try {
            UsuarioResponse usuario = administracionService.actualizarUsuario(rol, id, datos);
            return usuario == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/usuarios/{rol}/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String rol, @PathVariable Long id) {
        try {
            return administracionService.eliminarUsuario(rol, id)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}