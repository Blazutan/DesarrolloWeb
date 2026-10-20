package com.poli.polirestaurante.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poli.polirestaurante.dao.DetallePedidoDAO;
import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.dao.ProductoDAO;
import com.poli.polirestaurante.dto.DetallePedidoRequest;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.modelo.Producto;

@Service
@Transactional
public class DetallePedidoService {

    private final DetallePedidoDAO detallePedidoDAO;
    private final PedidoDAO pedidoDAO;
    private final ProductoDAO productoDAO;

    public DetallePedidoService(DetallePedidoDAO detallePedidoDAO, PedidoDAO pedidoDAO, ProductoDAO productoDAO) {
        this.detallePedidoDAO = detallePedidoDAO;
        this.pedidoDAO = pedidoDAO;
        this.productoDAO = productoDAO;
    }

    public List<DetallePedido> listar(String correo, boolean puedeVerTodos) {
        List<DetallePedido> detalles = detallePedidoDAO.listar();
        if (puedeVerTodos) {
            return detalles;
        }
        return detalles.stream()
                .filter(detalle -> perteneceAMesero(detalle.getPedido(), correo))
                .toList();
    }

    public DetallePedido buscarPorId(Long id, String correo, boolean puedeVerTodos) {
        DetallePedido detalle = detallePedidoDAO.buscarPorId(id);
        if (detalle == null || puedeVerTodos || perteneceAMesero(detalle.getPedido(), correo)) {
            return detalle;
        }
        return null;
    }

    public DetallePedido crear(DetallePedidoRequest datos, String correo) {
        validar(datos);
        Pedido pedido = buscarPedido(datos.pedidoId());
        validarPropietario(pedido, correo);
        validarNoCancelado(pedido);
        Producto producto = buscarProducto(datos.productoId());
        DetallePedido detalle = new DetallePedido(pedido, producto, datos.cantidad(), datos.notas());
        DetallePedido guardado = detallePedidoDAO.crear(detalle);
        pedido.agregarDetalle(guardado);
        pedido.setPrecioTotal(pedido.calcularTotal());
        pedido.setFechaUltimaActualizacion(LocalDateTime.now());
        pedidoDAO.actualizar(pedido);
        return guardado;
    }

    public DetallePedido actualizar(Long id, DetallePedidoRequest datos, String correo, boolean puedeVerTodos) {
        validar(datos);
        DetallePedido detalle = detallePedidoDAO.buscarPorId(id);
        if (detalle == null) {
            return null;
        }
        Pedido pedidoAnterior = detalle.getPedido();
        validarAcceso(pedidoAnterior, correo, puedeVerTodos);
        validarNoCancelado(pedidoAnterior);
        Pedido pedidoNuevo = buscarPedido(datos.pedidoId());
        validarAcceso(pedidoNuevo, correo, puedeVerTodos);
        validarNoCancelado(pedidoNuevo);
        Producto producto = buscarProducto(datos.productoId());
        detalle.setPedido(pedidoNuevo);
        detalle.setProducto(producto);
        detalle.setCantidad(datos.cantidad());
        detalle.setNotas(datos.notas());
        detalle.setPrecioUnitario(producto.getPrecio());
        DetallePedido actualizado = detallePedidoDAO.actualizar(detalle);
        if (!pedidoAnterior.getId().equals(pedidoNuevo.getId())) {
            pedidoAnterior.getDetalles().removeIf(item -> id.equals(item.getId()));
            pedidoNuevo.agregarDetalle(actualizado);
        } else {
            pedidoNuevo.getDetalles().stream().filter(item -> id.equals(item.getId())).findFirst()
                    .ifPresent(item -> {
                        item.setCantidad(datos.cantidad());
                        item.setProducto(producto);
                        item.setNotas(datos.notas());
                        item.setPrecioUnitario(producto.getPrecio());
                    });
        }
        actualizarTotal(pedidoAnterior);
        if (!pedidoAnterior.getId().equals(pedidoNuevo.getId())) {
            actualizarTotal(pedidoNuevo);
        }
        return actualizado;
    }

    public boolean eliminar(Long id, String correo, boolean puedeVerTodos) {
        DetallePedido detalle = detallePedidoDAO.buscarPorId(id);
        if (detalle == null) {
            return false;
        }
        Pedido pedido = detalle.getPedido();
        validarAcceso(pedido, correo, puedeVerTodos);
        validarNoCancelado(pedido);
        pedido.getDetalles().removeIf(item -> id.equals(item.getId()));
        detallePedidoDAO.eliminar(id);
        actualizarTotal(pedido);
        return true;
    }

    private void actualizarTotal(Pedido pedido) {
        pedido.setPrecioTotal(pedido.calcularTotal());
        pedido.setFechaUltimaActualizacion(LocalDateTime.now());
        pedidoDAO.actualizar(pedido);
    }

    private Pedido buscarPedido(Long id) {
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido == null) {
            throw new IllegalArgumentException("El pedido indicado no existe.");
        }
        return pedido;
    }

    private Producto buscarProducto(Long id) {
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            throw new IllegalArgumentException("El producto indicado no existe.");
        }
        if (!producto.isDisponibilidad()) {
            throw new IllegalArgumentException("El producto no está disponible.");
        }
        return producto;
    }

    private void validar(DetallePedidoRequest datos) {
        if (datos == null || datos.pedidoId() == null || datos.productoId() == null || datos.cantidad() <= 0) {
            throw new IllegalArgumentException("Pedido, producto y cantidad positiva son obligatorios.");
        }
    }

    private void validarPropietario(Pedido pedido, String correo) {
        if (!perteneceAMesero(pedido, correo)) {
            throw new IllegalArgumentException("El pedido no pertenece al mesero autenticado.");
        }
    }

    private void validarAcceso(Pedido pedido, String correo, boolean puedeVerTodos) {
        if (!puedeVerTodos) {
            validarPropietario(pedido, correo);
        }
    }

    private boolean perteneceAMesero(Pedido pedido, String correo) {
        return pedido.getMesero() != null && pedido.getMesero().getCorreo().equalsIgnoreCase(correo);
    }

    private void validarNoCancelado(Pedido pedido) {
        if ("CANCELADO".equalsIgnoreCase(pedido.getEstado().getNombre().trim())) {
            throw new IllegalArgumentException("No se pueden modificar los detalles de un pedido cancelado.");
        }
    }
}