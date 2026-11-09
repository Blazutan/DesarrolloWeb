package com.poli.polirestaurante.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReporteVentasResponse(
        LocalDate desde,
        LocalDate hasta,
        int totalPedidos,
        BigDecimal totalVentas,
        BigDecimal ticketPromedio,
        List<PedidoVendido> pedidos) {

    public record PedidoVendido(
            Long pedidoId,
            LocalDateTime fechaEntregado,
            Integer mesaNumero,
            BigDecimal total,
            List<DetalleVendido> detalles) {
    }

    public record DetalleVendido(
            Long productoId,
            String producto,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal) {
    }
}