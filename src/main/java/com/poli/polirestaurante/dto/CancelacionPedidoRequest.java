package com.poli.polirestaurante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelacionPedidoRequest(
        @NotBlank(message = "El motivo de cancelación es obligatorio.")
        @Size(max = 255, message = "El motivo de cancelación no puede superar 255 caracteres.")
        String motivo) {
}
