package com.poli.polirestaurante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poli.polirestaurante.dao.EstadoPedidoDAO;
import com.poli.polirestaurante.dao.MeseroDAO;
import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoDAO pedidoDAO;

    @Mock
    private MeseroDAO meseroDAO;

    @Mock
    private EstadoPedidoDAO estadoPedidoDAO;

    @InjectMocks
    private PedidoService pedidoService;

    private Mesero mesero;
    private EstadoPedido pendiente;
    private EstadoPedido enPreparacion;
    private EstadoPedido listo;
    private EstadoPedido entregado;
    private EstadoPedido cancelado;

    @BeforeEach
    void setUp() {
        mesero = new Mesero("Ana Ruiz", "ana@polirestaurante.com", "hash");
        mesero.setId(1L);
        pendiente = new EstadoPedido("PENDIENTE");
        enPreparacion = new EstadoPedido("EN PREPARACIÓN");
        listo = new EstadoPedido("LISTO");
        entregado = new EstadoPedido("ENTREGADO");
        cancelado = new EstadoPedido("CANCELADO");
    }

    private void configurarEstados() {
        when(estadoPedidoDAO.listar()).thenReturn(List.of(pendiente, enPreparacion, listo, entregado, cancelado));
    }

    @Test
    void crear_asociaElMeseroYDejaElPedidoPendienteConFechaDeCreacion() {
        configurarEstados();
        when(meseroDAO.buscarPorId(1L)).thenReturn(mesero);
        when(pedidoDAO.crear(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Pedido resultado = pedidoService.crear(1L);

        assertThat(resultado.getMesero()).isSameAs(mesero);
        assertThat(resultado.getEstado()).isSameAs(pendiente);
        assertThat(resultado.getFechaCreacion()).isNotNull();
    }

    @Test
    void listarPendientes_delegaLaColaOrdenadaAlDao() {
        List<Pedido> cola = List.of(new Pedido(mesero, pendiente));
        when(pedidoDAO.listarPendientes()).thenReturn(cola);

        assertThat(pedidoService.listarPendientes()).isSameAs(cola);
        verify(pedidoDAO).listarPendientes();
    }

    @Test
    void cambiarEstado_registraLasFechasDePreparacionListoYEntrega() {
        configurarEstados();
        Pedido pedido = new Pedido(mesero, pendiente);
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        pedidoService.cambiarEstado(10L, "EN PREPARACIÓN");
        LocalDateTime fechaPreparacion = pedido.getFechaEnPreparacion();
        pedidoService.cambiarEstado(10L, "LISTO");
        LocalDateTime fechaListo = pedido.getFechaListo();
        pedidoService.cambiarEstado(10L, "ENTREGADO");

        assertThat(pedido.getEstado()).isSameAs(entregado);
        assertThat(fechaPreparacion).isNotNull();
        assertThat(fechaListo).isNotNull();
        assertThat(pedido.getFechaEntregado()).isNotNull();
        assertThat(fechaListo).isAfterOrEqualTo(fechaPreparacion);
        assertThat(pedido.getFechaEntregado()).isAfterOrEqualTo(fechaListo);
    }

    @Test
    void cambiarEstado_rechazaSaltarEstados() {
        Pedido pedido = new Pedido(mesero, pendiente);
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);

        assertThatThrownBy(() -> pedidoService.cambiarEstado(10L, "LISTO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelar_guardaElMotivoAntesDeLaEntrega() {
        configurarEstados();
        Pedido pedido = new Pedido(mesero, listo);
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Pedido resultado = pedidoService.cancelar(10L, "  Cliente se retiró  ");

        assertThat(resultado.getEstado()).isSameAs(cancelado);
        assertThat(resultado.getMotivoCancelacion()).isEqualTo("Cliente se retiró");
    }

    @Test
    void cancelar_rechazaPedidosEntregados() {
        Pedido pedido = new Pedido(mesero, entregado);
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);

        assertThatThrownBy(() -> pedidoService.cancelar(10L, "Error"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}