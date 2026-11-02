package com.poli.polirestaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.dto.DetallePedidoRequest;
import com.poli.polirestaurante.dto.DetallePedidoResponse;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.service.DetallePedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/detalles-pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping
    public List<DetallePedidoResponse> listar(@AuthenticationPrincipal UserDetails usuario) {
        boolean puedeVerTodos = tieneRol(usuario, "COCINA") || tieneRol(usuario, "ADMINISTRADOR");
        return detallePedidoService.listar(usuario.getUsername(), puedeVerTodos).stream()
                .map(DetallePedidoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetallePedidoResponse> buscarPorId(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails usuario) {
        boolean puedeVerTodos = tieneRol(usuario, "COCINA") || tieneRol(usuario, "ADMINISTRADOR");
        DetallePedido detalle = detallePedidoService.buscarPorId(id, usuario.getUsername(), puedeVerTodos);
        return detalle == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(DetallePedidoResponse.desde(detalle));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody DetallePedidoRequest datos,
            @AuthenticationPrincipal UserDetails usuario) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(DetallePedidoResponse.desde(detallePedidoService.crear(datos, usuario.getUsername())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody DetallePedidoRequest datos,
            @AuthenticationPrincipal UserDetails usuario) {
        try {
            boolean puedeVerTodos = tieneRol(usuario, "ADMINISTRADOR");
            DetallePedido detalle = detallePedidoService.actualizar(id, datos, usuario.getUsername(), puedeVerTodos);
            return detalle == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(DetallePedidoResponse.desde(detalle));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, @AuthenticationPrincipal UserDetails usuario) {
        try {
            boolean puedeVerTodos = tieneRol(usuario, "ADMINISTRADOR");
            return detallePedidoService.eliminar(id, usuario.getUsername(), puedeVerTodos)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean tieneRol(UserDetails usuario, String rol) {
        return usuario.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_" + rol));
    }
}