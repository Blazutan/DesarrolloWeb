package com.poli.polirestaurante.dto;

import java.time.LocalDateTime;

import com.poli.polirestaurante.modelo.Pedido;

public record PedidoResponse(
        Long id,
        Long meseroId,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaUltimaActualizacion,
        LocalDateTime fechaEnPreparacion,
        LocalDateTime fechaListo,
        LocalDateTime fechaEntregado,
        java.math.BigDecimal precioTotal,
        int numeroRecordatorios,
        boolean alertaRecibida) {

    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getMesero().getId(), pedido.getEstado().getNombre(),
            pedido.getFechaCreacion(), pedido.getFechaUltimaActualizacion(), pedido.getFechaEnPreparacion(),
            pedido.getFechaListo(), pedido.getFechaEntregado(), pedido.getPrecioTotal(),
            pedido.getNumeroRecordatorios(), pedido.isAlertaRecibida());
    }
}