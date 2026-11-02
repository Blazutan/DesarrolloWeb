package com.poli.polirestaurante.dto;

import jakarta.validation.constraints.NotNull;

public record PedidoUpdateRequest(@NotNull Long meseroId, Long cocinaId) {
}