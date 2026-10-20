package com.poli.polirestaurante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poli.polirestaurante.dao.DetallePedidoDAO;
import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.dao.ProductoDAO;
import com.poli.polirestaurante.dto.DetallePedidoRequest;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.modelo.Producto;

@ExtendWith(MockitoExtension.class)
class DetallePedidoServiceTest {

    @Mock
    private DetallePedidoDAO detallePedidoDAO;

    @Mock
    private PedidoDAO pedidoDAO;

    @Mock
    private ProductoDAO productoDAO;

    @InjectMocks
    private DetallePedidoService detallePedidoService;

    @Test
    void crear_asociaDetalleYCongelaPrecioEnElTotalDelPedido() {
        Mesero mesero = new Mesero("Ana", "ana@restaurante.com", "hash");
        Pedido pedido = new Pedido(mesero, new EstadoPedido("PENDIENTE"));
        pedido.setId(5L);
        Producto producto = new Producto("Jugo", "Natural", new BigDecimal("3500"), true, null);
        producto.setId(8L);
        when(pedidoDAO.buscarPorId(5L)).thenReturn(pedido);
        when(productoDAO.buscarPorId(8L)).thenReturn(producto);
        when(detallePedidoDAO.crear(any(DetallePedido.class))).thenAnswer(invocacion -> {
            DetallePedido detalle = invocacion.getArgument(0);
            detalle.setId(9L);
            return detalle;
        });
        when(pedidoDAO.actualizar(pedido)).thenReturn(pedido);

        DetallePedido detalle = detallePedidoService.crear(
                new DetallePedidoRequest(5L, 8L, 2, "Sin azúcar"), "ana@restaurante.com");

        assertThat(detalle.getPedido()).isSameAs(pedido);
        assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo("3500");
        assertThat(detalle.calcularSubtotal()).isEqualByComparingTo("7000");
        assertThat(pedido.getPrecioTotal()).isEqualByComparingTo("7000");
    }

    @Test
    void crear_rechazaAgregarDetallesAUnPedidoCancelado() {
        Mesero mesero = new Mesero("Ana", "ana@restaurante.com", "hash");
        Pedido pedido = new Pedido(mesero, new EstadoPedido("CANCELADO"));
        pedido.setId(5L);
        when(pedidoDAO.buscarPorId(5L)).thenReturn(pedido);

        assertThatThrownBy(() -> detallePedidoService.crear(
                new DetallePedidoRequest(5L, 8L, 1, null), "ana@restaurante.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se pueden modificar los detalles de un pedido cancelado.");
    }
}