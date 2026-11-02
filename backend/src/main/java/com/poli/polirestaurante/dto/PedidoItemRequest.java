package com.poli.polirestaurante.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PedidoItemRequest(
        @NotNull Long productoId,
        @Positive int cantidad,
        @Size(max = 255) String notas) {
}
