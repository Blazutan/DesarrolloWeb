package com.poli.polirestaurante.dto;

import java.math.BigDecimal;

import com.poli.polirestaurante.modelo.DetallePedido;

public record DetallePedidoResponse(
        Long id,
        Long pedidoId,
        Long productoId,
        String productoNombre,
        int cantidad,
        String notas,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {

    public static DetallePedidoResponse desde(DetallePedido detalle) {
        return new DetallePedidoResponse(detalle.getId(), detalle.getPedido().getId(), detalle.getProducto().getId(),
                detalle.getProducto().getNombre(), detalle.getCantidad(), detalle.getNotas(),
                detalle.getPrecioUnitario(), detalle.calcularSubtotal());
    }
}