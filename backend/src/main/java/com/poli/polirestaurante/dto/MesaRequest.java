package com.poli.polirestaurante.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MesaRequest(@Min(1) int numero, @NotBlank @Size(max = 20) String estado) {
}