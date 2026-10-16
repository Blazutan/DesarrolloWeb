package com.poli.polirestaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Pedido> crear(@RequestParam Long meseroId) {
        Pedido pedido = pedidoService.crear(meseroId);
        return pedido == null ? ResponseEntity.notFound().build()
                : ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/cola/cocina")
    public List<Pedido> listarPendientes() {
        return pedidoService.listarPendientes();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        try {
            Pedido pedido = pedidoService.cambiarEstado(id, estado);
            return pedido == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancelacion")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @RequestParam String motivo) {
        try {
            Pedido pedido = pedidoService.cancelar(id, motivo);
            return pedido == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(pedido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}