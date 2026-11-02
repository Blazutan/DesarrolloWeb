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
        boolean alertaRecibida,
        String motivoCancelacion,
        Long mesaId,
        Integer mesaNumero) {

    public static PedidoResponse desde(Pedido pedido) {
        var mesa = pedido.getMesas().stream().findFirst().orElse(null);
        return new PedidoResponse(pedido.getId(), pedido.getMesero().getId(), pedido.getEstado().getNombre(),
            pedido.getFechaCreacion(), pedido.getFechaUltimaActualizacion(), pedido.getFechaEnPreparacion(),
            pedido.getFechaListo(), pedido.getFechaEntregado(), pedido.getPrecioTotal(),
            pedido.getNumeroRecordatorios(), pedido.isAlertaRecibida(), pedido.getMotivoCancelacion(),
            mesa == null ? null : mesa.getId(), mesa == null ? null : mesa.getNumero());
    }
}