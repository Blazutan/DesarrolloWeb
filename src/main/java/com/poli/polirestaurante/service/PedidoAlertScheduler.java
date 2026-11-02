package com.poli.polirestaurante.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PedidoAlertScheduler {

    private final PedidoService pedidoService;

    public PedidoAlertScheduler(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Scheduled(fixedDelay = PedidoService.INTERVALO_RECORDATORIO_MS)
    public void enviarRecordatoriosVencidos() {
        pedidoService.enviarRecordatoriosVencidos();
    }
}