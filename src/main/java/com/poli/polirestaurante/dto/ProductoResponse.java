package com.poli.polirestaurante.dto;

import java.math.BigDecimal;

import com.poli.polirestaurante.modelo.Producto;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        boolean disponibilidad,
        Long categoriaId,
        String categoriaNombre) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getPrecio(), producto.isDisponibilidad(),
                producto.getCategoria() == null ? null : producto.getCategoria().getId(),
                producto.getCategoria() == null ? null : producto.getCategoria().getNombre());
    }
}