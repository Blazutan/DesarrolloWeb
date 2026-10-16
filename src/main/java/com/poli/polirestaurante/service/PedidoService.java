package com.poli.polirestaurante.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.poli.polirestaurante.dao.EstadoPedidoDAO;
import com.poli.polirestaurante.dao.MeseroDAO;
import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;

@Service
public class PedidoService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String EN_PREPARACION = "EN PREPARACION";
    private static final String LISTO = "LISTO";
    private static final String ENTREGADO = "ENTREGADO";
    private static final String CANCELADO = "CANCELADO";

    private final PedidoDAO pedidoDAO;
    private final MeseroDAO meseroDAO;
    private final EstadoPedidoDAO estadoPedidoDAO;

    public PedidoService(PedidoDAO pedidoDAO, MeseroDAO meseroDAO, EstadoPedidoDAO estadoPedidoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.meseroDAO = meseroDAO;
        this.estadoPedidoDAO = estadoPedidoDAO;
    }

    public Pedido crear(Long meseroId) {
        Mesero mesero = meseroDAO.buscarPorId(meseroId);
        if (mesero == null) {
            return null;
        }
        return pedidoDAO.crear(new Pedido(mesero, buscarEstado(PENDIENTE)));
    }

    public List<Pedido> listarPendientes() {
        return pedidoDAO.listarPendientes();
    }

    public Pedido cambiarEstado(Long id, String nombreEstado) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }

        String estadoActual = normalizar(pedido.getEstado().getNombre());
        String estadoDestino = normalizar(nombreEstado);
        boolean transicionValida = (PENDIENTE.equals(estadoActual) && EN_PREPARACION.equals(estadoDestino))
                || (EN_PREPARACION.equals(estadoActual) && LISTO.equals(estadoDestino))
                || (LISTO.equals(estadoActual) && ENTREGADO.equals(estadoDestino));
        if (!transicionValida) {
            throw new IllegalArgumentException("La transición de estado del pedido no está permitida.");
        }

        pedido.setEstado(buscarEstado(estadoDestino));
        LocalDateTime ahora = LocalDateTime.now();
        switch (estadoDestino) {
            case EN_PREPARACION -> pedido.setFechaEnPreparacion(ahora);
            case LISTO -> pedido.setFechaListo(ahora);
            case ENTREGADO -> pedido.setFechaEntregado(ahora);
            default -> throw new IllegalArgumentException("Estado de pedido no válido.");
        }
        return pedidoDAO.actualizar(pedido);
    }

    public Pedido cancelar(Long id, String motivo) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
        }

        String estadoActual = normalizar(pedido.getEstado().getNombre());
        if (ENTREGADO.equals(estadoActual) || CANCELADO.equals(estadoActual)) {
            throw new IllegalArgumentException("Solo se puede cancelar un pedido que no haya sido entregado.");
        }

        pedido.setEstado(buscarEstado(CANCELADO));
        pedido.setMotivoCancelacion(motivo.trim());
        pedido.setFechaCancelacion(LocalDateTime.now());
        return pedidoDAO.actualizar(pedido);
    }

    private EstadoPedido buscarEstado(String nombreNormalizado) {
        return estadoPedidoDAO.listar().stream()
                .filter(estado -> normalizar(estado.getNombre()).equals(nombreNormalizado))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No está configurado el estado " + nombreNormalizado + "."));
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }
}