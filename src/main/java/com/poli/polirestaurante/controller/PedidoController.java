package com.poli.polirestaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.dto.AlertaPedidoResponse;
import com.poli.polirestaurante.dto.CancelacionPedidoRequest;
import com.poli.polirestaurante.dto.PedidoResponse;
import com.poli.polirestaurante.dto.PedidoUpdateRequest;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@AuthenticationPrincipal UserDetails usuario) {
        Pedido pedido = pedidoService.crear(usuario.getUsername());
        return pedido == null ? ResponseEntity.notFound().build()
                : ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.desde(pedido));
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidoService.listar().stream().map(PedidoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);
        return pedido == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(PedidoResponse.desde(pedido));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarAdministrativo(@PathVariable Long id,
            @Valid @RequestBody PedidoUpdateRequest datos) {
        try {
            Pedido pedido = pedidoService.actualizarAdministrativo(id, datos.meseroId(), datos.cocinaId());
            return pedido == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(PedidoResponse.desde(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return pedidoService.eliminar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/cola/cocina")
    public List<PedidoResponse> listarPendientes() {
        return pedidoService.listarPendientes().stream().map(PedidoResponse::desde).toList();
    }

    @GetMapping("/alertas")
    public List<AlertaPedidoResponse> listarAlertas(@AuthenticationPrincipal UserDetails usuario) {
        return pedidoService.listarAlertas(usuario.getUsername()).stream()
                .map(AlertaPedidoResponse::desde).toList();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            Pedido pedido = pedidoService.cambiarEstadoCocina(id, estado);
            return pedido == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(PedidoResponse.desde(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/entrega")
    public ResponseEntity<?> entregar(@PathVariable Long id, @AuthenticationPrincipal UserDetails usuario) {
        try {
            Pedido pedido = pedidoService.entregar(id, usuario.getUsername());
            return pedido == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(PedidoResponse.desde(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<?> cancelar(@PathVariable Long id,
            @Valid @RequestBody CancelacionPedidoRequest datos,
            @AuthenticationPrincipal UserDetails usuario) {
        try {
            Pedido pedido = pedidoService.cancelar(id, datos.motivo(), usuario.getUsername());
            return pedido == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(PedidoResponse.desde(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/recibido")
    public ResponseEntity<?> marcarAlertaRecibida(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails usuario) {
        try {
            Pedido pedido = pedidoService.marcarAlertaRecibida(id, usuario.getUsername());
            return pedido == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(PedidoResponse.desde(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}