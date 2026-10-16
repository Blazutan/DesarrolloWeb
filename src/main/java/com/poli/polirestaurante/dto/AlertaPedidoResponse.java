package com.poli.polirestaurante.dto;

import java.time.LocalDateTime;

import com.poli.polirestaurante.modelo.Pedido;

public record AlertaPedidoResponse(Long pedidoId, LocalDateTime fechaListo, int numeroRecordatorios) {

    public static AlertaPedidoResponse desde(Pedido pedido) {
        return new AlertaPedidoResponse(pedido.getId(), pedido.getFechaListo(), pedido.getNumeroRecordatorios());
    }
}