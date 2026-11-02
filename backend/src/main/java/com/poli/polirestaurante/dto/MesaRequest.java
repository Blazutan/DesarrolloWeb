package com.poli.polirestaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MesaRequest(@Min(1) int numero, @NotBlank String estado) {
}