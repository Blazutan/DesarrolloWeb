package com.poli.polirestaurante.dto;

import java.time.LocalDate;

public record ReporteTiemposResponse(
        LocalDate desde,
        LocalDate hasta,
        int pedidosAnalizados,
        Long promedioPreparacionSegundos,
        Long promedioEsperaEntregaSegundos,
        Long promedioTotalSegundos) {
}