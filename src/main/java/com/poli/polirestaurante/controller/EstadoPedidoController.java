package com.poli.polirestaurante.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poli.polirestaurante.dao.EstadoPedidoDAO;
import com.poli.polirestaurante.modelo.EstadoPedido;

@RestController
@RequestMapping("/api/estados-pedido")
public class EstadoPedidoController {

    private final EstadoPedidoDAO estadoPedidoDAO;

    public EstadoPedidoController(EstadoPedidoDAO estadoPedidoDAO) {
        this.estadoPedidoDAO = estadoPedidoDAO;
    }

    @GetMapping
    public List<EstadoPedido> listar() {
        return estadoPedidoDAO.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadoPedido> buscarPorId(@PathVariable Long id) {
        EstadoPedido estado = estadoPedidoDAO.buscarPorId(id);
        return estado == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(estado);
    }

    @PostMapping
    public ResponseEntity<EstadoPedido> crear(@RequestBody EstadoPedido estado) {
        estado.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(estadoPedidoDAO.crear(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadoPedido> actualizar(@PathVariable Long id, @RequestBody EstadoPedido datos) {
        EstadoPedido estado = estadoPedidoDAO.buscarPorId(id);
        if (estado == null) {
            return ResponseEntity.notFound().build();
        }
        estado.setNombre(datos.getNombre());
        return ResponseEntity.ok(estadoPedidoDAO.actualizar(estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return estadoPedidoDAO.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}