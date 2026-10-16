package com.poli.polirestaurante.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poli.polirestaurante.dao.CocinaDAO;
import com.poli.polirestaurante.dao.EstadoPedidoDAO;
import com.poli.polirestaurante.dao.MeseroDAO;
import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;

@Service
@Transactional
public class PedidoService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String EN_PREPARACION = "EN PREPARACION";
    private static final String LISTO = "LISTO";
    private static final String ENTREGADO = "ENTREGADO";
    public static final int MAXIMO_RECORDATORIOS = 3;
    public static final long INTERVALO_RECORDATORIO_MS = 60_000;

    private final PedidoDAO pedidoDAO;
    private final MeseroDAO meseroDAO;
    private final CocinaDAO cocinaDAO;
    private final EstadoPedidoDAO estadoPedidoDAO;

    public PedidoService(PedidoDAO pedidoDAO, MeseroDAO meseroDAO, CocinaDAO cocinaDAO,
            EstadoPedidoDAO estadoPedidoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.meseroDAO = meseroDAO;
        this.cocinaDAO = cocinaDAO;
        this.estadoPedidoDAO = estadoPedidoDAO;
    }

    public Pedido crear(String correoMesero) {
        Mesero mesero = meseroDAO.buscarPorCorreo(correoMesero);
        if (mesero == null) {
            return null;
        }
        return pedidoDAO.crear(new Pedido(mesero, buscarEstado(PENDIENTE)));
    }

    public List<Pedido> listarPendientes() {
        return pedidoDAO.listarPendientes();
    }

    public List<Pedido> listar() {
        return pedidoDAO.listar();
    }

    public Pedido buscarPorId(Long id) {
        return pedidoDAO.buscarPorId(id);
    }

    public Pedido actualizarAdministrativo(Long id, Long meseroId, Long cocinaId) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }
        if (meseroId != null) {
            Mesero mesero = meseroDAO.buscarPorId(meseroId);
            if (mesero == null) {
                throw new IllegalArgumentException("El mesero indicado no existe.");
            }
            pedido.setMesero(mesero);
        }
        if (cocinaId != null) {
            var cocina = cocinaDAO.buscarPorId(cocinaId);
            if (cocina == null) {
                throw new IllegalArgumentException("El usuario de cocina indicado no existe.");
            }
            pedido.setCocina(cocina);
        }
        pedido.setFechaUltimaActualizacion(LocalDateTime.now());
        return pedidoDAO.actualizar(pedido);
    }

    public boolean eliminar(Long id) {
        return pedidoDAO.eliminar(id);
    }

    public List<Pedido> listarAlertas(String correoMesero) {
        return pedidoDAO.listarAlertas(correoMesero);
    }

    public Pedido marcarAlertaRecibida(Long id, String correoMesero) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }
        validarMeseroResponsable(pedido, correoMesero);
        if (!LISTO.equals(normalizar(pedido.getEstado().getNombre()))) {
            throw new IllegalArgumentException("Solo se puede recibir la alerta de un pedido listo.");
        }

        pedido.setAlertaRecibida(true);
        pedido.setFechaProximoRecordatorio(null);
        pedido.setFechaUltimaActualizacion(LocalDateTime.now());
        return pedidoDAO.actualizar(pedido);
    }

    public void enviarRecordatoriosVencidos() {
        LocalDateTime ahora = LocalDateTime.now();
        for (Pedido pedido : pedidoDAO.listarRecordatoriosVencidos(ahora, MAXIMO_RECORDATORIOS)) {
            int recordatorios = pedido.getNumeroRecordatorios() + 1;
            pedido.setNumeroRecordatorios(recordatorios);
            pedido.setFechaProximoRecordatorio(recordatorios < MAXIMO_RECORDATORIOS
                    ? ahora.plusNanos(INTERVALO_RECORDATORIO_MS * 1_000_000)
                    : null);
            pedidoDAO.actualizar(pedido);
        }
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
        pedido.setFechaUltimaActualizacion(ahora);
        switch (estadoDestino) {
            case EN_PREPARACION -> pedido.setFechaEnPreparacion(ahora);
            case LISTO -> {
                pedido.setFechaListo(ahora);
                pedido.setAlertaRecibida(false);
                pedido.setNumeroRecordatorios(1);
                pedido.setFechaProximoRecordatorio(ahora.plusNanos(INTERVALO_RECORDATORIO_MS * 1_000_000));
            }
            case ENTREGADO -> pedido.setFechaEntregado(ahora);
            default -> throw new IllegalArgumentException("Estado de pedido no válido.");
        }
        return pedidoDAO.actualizar(pedido);
    }

    public Pedido cambiarEstadoCocina(Long id, String nombreEstado) {
        String estadoDestino = normalizar(nombreEstado);
        if (!EN_PREPARACION.equals(estadoDestino) && !LISTO.equals(estadoDestino)) {
            throw new IllegalArgumentException("Cocina solo puede marcar un pedido en preparación o listo.");
        }
        return cambiarEstado(id, nombreEstado);
    }

    public Pedido entregar(Long id, String correoMesero) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }
        validarMeseroResponsable(pedido, correoMesero);
        if (!LISTO.equals(normalizar(pedido.getEstado().getNombre()))) {
            throw new IllegalArgumentException("Solo se puede entregar un pedido que esté listo.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        pedido.setEstado(buscarEstado(ENTREGADO));
        pedido.setFechaEntregado(ahora);
        pedido.setAlertaRecibida(true);
        pedido.setFechaProximoRecordatorio(null);
        return pedidoDAO.actualizar(pedido);
    }

    private void validarMeseroResponsable(Pedido pedido, String correoMesero) {
        if (pedido.getMesero() == null || !pedido.getMesero().getCorreo().equalsIgnoreCase(correoMesero)) {
            throw new IllegalArgumentException("El pedido no pertenece al mesero autenticado.");
        }
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