package com.poli.polirestaurante.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull Long mesaId,
        @NotEmpty @Valid List<PedidoItemRequest> detalles) {
}
