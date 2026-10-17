package com.poli.polirestaurante.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteVentasResponse(
        LocalDate desde,
        LocalDate hasta,
        int totalPedidos,
        BigDecimal totalVentas,
        BigDecimal ticketPromedio,
        List<ProductoVendido> productos) {

    public record ProductoVendido(String producto, long unidades, BigDecimal total) {
    }
}