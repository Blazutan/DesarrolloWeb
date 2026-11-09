package com.poli.polirestaurante.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poli.polirestaurante.dao.CocinaDAO;
import com.poli.polirestaurante.dao.DetallePedidoDAO;
import com.poli.polirestaurante.dao.EstadoPedidoDAO;
import com.poli.polirestaurante.dao.MesaDAO;
import com.poli.polirestaurante.dao.MeseroDAO;
import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.dao.ProductoDAO;
import com.poli.polirestaurante.dto.PedidoItemRequest;
import com.poli.polirestaurante.dto.PedidoRequest;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.Mesa;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.modelo.Producto;

@Service
@Transactional
public class PedidoService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String EN_PREPARACION = "EN PREPARACION";
    private static final String LISTO = "LISTO";
    private static final String ENTREGADO = "ENTREGADO";
    private static final String CANCELADO = "CANCELADO";
    public static final int MAXIMO_RECORDATORIOS = 3;
    public static final long INTERVALO_RECORDATORIO_MS = 60_000;

    private final PedidoDAO pedidoDAO;
    private final MeseroDAO meseroDAO;
    private final CocinaDAO cocinaDAO;
    private final EstadoPedidoDAO estadoPedidoDAO;
    private final MesaDAO mesaDAO;
    private final ProductoDAO productoDAO;
    private final DetallePedidoDAO detallePedidoDAO;

    public PedidoService(PedidoDAO pedidoDAO, MeseroDAO meseroDAO, CocinaDAO cocinaDAO,
            EstadoPedidoDAO estadoPedidoDAO, MesaDAO mesaDAO, ProductoDAO productoDAO,
            DetallePedidoDAO detallePedidoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.meseroDAO = meseroDAO;
        this.cocinaDAO = cocinaDAO;
        this.estadoPedidoDAO = estadoPedidoDAO;
        this.mesaDAO = mesaDAO;
        this.productoDAO = productoDAO;
        this.detallePedidoDAO = detallePedidoDAO;
    }

    public Pedido crear(String correoMesero, PedidoRequest datos) {
        validarPedido(datos);
        Mesero mesero = meseroDAO.buscarPorCorreo(correoMesero);
        if (mesero == null) {
            return null;
        }
        Mesa mesa = mesaDAO.buscarPorId(datos.mesaId());
        if (mesa == null) {
            return null;
        }
        if (!"LIBRE".equalsIgnoreCase(mesa.getEstado()) || mesa.getPedido() != null) {
            throw new IllegalStateException("La mesa seleccionada ya no está disponible.");
        }

        Pedido pedido = pedidoDAO.crear(new Pedido(mesero, buscarEstado(PENDIENTE)));
        for (PedidoItemRequest item : datos.detalles()) {
            Producto producto = productoDAO.buscarPorId(item.productoId());
            if (producto == null) {
                throw new IllegalArgumentException("Uno de los productos indicados no existe.");
            }
            if (!producto.isDisponibilidad()) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no está disponible.");
            }
            DetallePedido detalle = detallePedidoDAO.crear(
                    new DetallePedido(pedido, producto, item.cantidad(), item.notas()));
            pedido.agregarDetalle(detalle);
        }
        pedido.setPrecioTotal(pedido.calcularTotal());
        mesa.ocupar();
        mesa.setPedido(pedido);
        pedido.getMesas().add(mesa);
        mesaDAO.actualizar(mesa);
        return pedidoDAO.actualizar(pedido);
    }

    public List<Pedido> listarPorMesero(String correoMesero) {
        return pedidoDAO.listarPorMesero(correoMesero).stream()
                .filter(pedido -> {
                    String estado = normalizar(pedido.getEstado().getNombre());
                    return !ENTREGADO.equals(estado) && !CANCELADO.equals(estado);
                })
                .toList();
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
        validarNoCancelado(pedido);
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

    public Pedido cancelar(Long id, String motivo, String correoMesero) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }
        validarMeseroResponsable(pedido, correoMesero);
        return cancelarPedido(pedido, motivo);
    }

    public Pedido cancelarPorCocina(Long id, String motivo) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            return null;
        }
        return cancelarPedido(pedido, motivo);
    }

    private Pedido cancelarPedido(Pedido pedido, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de cancelación es obligatorio.");
        }
        String motivoNormalizado = motivo.trim();
        if (motivoNormalizado.length() > 255) {
            throw new IllegalArgumentException("El motivo de cancelación no puede superar 255 caracteres.");
        }
        String estadoActual = normalizar(pedido.getEstado().getNombre());
        if (ENTREGADO.equals(estadoActual) || CANCELADO.equals(estadoActual)) {
            throw new IllegalArgumentException("No se puede cancelar un pedido entregado o ya cancelado.");
        }

        pedido.setEstado(buscarEstado(CANCELADO));
        pedido.setMotivoCancelacion(motivoNormalizado);
        pedido.setFechaUltimaActualizacion(LocalDateTime.now());
        pedido.setAlertaRecibida(true);
        pedido.setFechaProximoRecordatorio(null);
        liberarMesa(pedido);
        return pedidoDAO.actualizar(pedido);
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
        pedido.setFechaUltimaActualizacion(ahora);
        pedido.setAlertaRecibida(true);
        pedido.setFechaProximoRecordatorio(null);
        liberarMesa(pedido);
        return pedidoDAO.actualizar(pedido);
    }

    private void validarPedido(PedidoRequest datos) {
        if (datos == null || datos.mesaId() == null || datos.detalles() == null || datos.detalles().isEmpty()) {
            throw new IllegalArgumentException("La mesa y al menos un producto son obligatorios.");
        }
        if (datos.detalles().stream().anyMatch(item -> item == null || item.productoId() == null
                || item.cantidad() <= 0)) {
            throw new IllegalArgumentException("Cada detalle requiere un producto y una cantidad positiva.");
        }
    }

    private void liberarMesa(Pedido pedido) {
        mesaDAO.buscarPorPedidoId(pedido.getId()).ifPresent(mesa -> {
            mesa.liberar();
            mesaDAO.actualizar(mesa);
            pedido.getMesas().removeIf(ocupada -> ocupada.getId().equals(mesa.getId()));
        });
    }

    private void validarMeseroResponsable(Pedido pedido, String correoMesero) {
        if (pedido.getMesero() == null || !pedido.getMesero().getCorreo().equalsIgnoreCase(correoMesero)) {
            throw new IllegalArgumentException("El pedido no pertenece al mesero autenticado.");
        }
    }

    private void validarNoCancelado(Pedido pedido) {
        if (CANCELADO.equals(normalizar(pedido.getEstado().getNombre()))) {
            throw new IllegalArgumentException("No se puede modificar un pedido cancelado.");
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