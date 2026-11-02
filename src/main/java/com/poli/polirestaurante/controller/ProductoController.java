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

import com.poli.polirestaurante.dto.ProductoRequest;
import com.poli.polirestaurante.dto.ProductoResponse;
import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.listar().stream().map(ProductoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable Long id) {
        Producto producto = productoService.buscarPorId(id);
        return producto == null ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(ProductoResponse.desde(producto));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProductoRequest datos) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ProductoResponse.desde(productoService.crear(datos)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        try {
            Producto producto = productoService.actualizar(id, datos);
            return producto == null ? ResponseEntity.notFound().build()
                    : ResponseEntity.ok(ProductoResponse.desde(producto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return productoService.eliminar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}