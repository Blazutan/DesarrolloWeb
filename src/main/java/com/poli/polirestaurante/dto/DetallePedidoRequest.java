package com.poli.polirestaurante.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetallePedidoRequest(
        @NotNull Long pedidoId,
        @NotNull Long productoId,
        @Positive int cantidad,
        String notas) {
}