package com.poli.polirestaurante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.poli.polirestaurante.dao.CocinaDAO;
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
    private CocinaDAO cocinaDAO;

    @Mock
    private EstadoPedidoDAO estadoPedidoDAO;

    @InjectMocks
    private PedidoService pedidoService;

    private Mesero mesero;
    private EstadoPedido pendiente;
    private EstadoPedido enPreparacion;
    private EstadoPedido listo;
    private EstadoPedido entregado;

    @BeforeEach
    void setUp() {
        mesero = new Mesero("Ana Ruiz", "ana@polirestaurante.com", "hash");
        mesero.setId(1L);
        pendiente = new EstadoPedido("PENDIENTE");
        enPreparacion = new EstadoPedido("EN PREPARACIÓN");
        listo = new EstadoPedido("LISTO");
        entregado = new EstadoPedido("ENTREGADO");
    }

    private void configurarEstados() {
        when(estadoPedidoDAO.listar()).thenReturn(List.of(pendiente, enPreparacion, listo, entregado));
    }

    @Test
    void crear_asociaElMeseroYDejaElPedidoPendienteConFechaDeCreacion() {
        configurarEstados();
        when(meseroDAO.buscarPorCorreo(mesero.getCorreo())).thenReturn(mesero);
        when(pedidoDAO.crear(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Pedido resultado = pedidoService.crear(mesero.getCorreo());

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
        assertThat(pedido.getNumeroRecordatorios()).isEqualTo(1);
        assertThat(pedido.isAlertaRecibida()).isFalse();
        assertThat(pedido.getFechaProximoRecordatorio()).isNotNull();
    }

    @Test
    void cambiarEstado_rechazaSaltarEstados() {
        Pedido pedido = new Pedido(mesero, pendiente);
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);

        assertThatThrownBy(() -> pedidoService.cambiarEstado(10L, "LISTO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void enviarRecordatoriosVencidos_noSuperaTresYDesactivaElSiguienteAlLlegarAlMaximo() {
        Pedido pedido = new Pedido(mesero, listo);
        pedido.setNumeroRecordatorios(2);
        when(pedidoDAO.listarRecordatoriosVencidos(any(LocalDateTime.class), eq(3))).thenReturn(List.of(pedido));

        pedidoService.enviarRecordatoriosVencidos();

        assertThat(pedido.getNumeroRecordatorios()).isEqualTo(3);
        assertThat(pedido.getFechaProximoRecordatorio()).isNull();
        verify(pedidoDAO).actualizar(pedido);
    }

    @Test
    void marcarAlertaRecibida_detieneLosSiguientesRecordatorios() {
        Pedido pedido = new Pedido(mesero, listo);
        pedido.setFechaProximoRecordatorio(LocalDateTime.now().plusMinutes(1));
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Pedido resultado = pedidoService.marcarAlertaRecibida(10L, mesero.getCorreo());

        assertThat(resultado.isAlertaRecibida()).isTrue();
        assertThat(resultado.getFechaProximoRecordatorio()).isNull();
    }

    @Test
    void marcarAlertaRecibida_rechazaAUnMeseroQueNoEsElResponsable() {
        Pedido pedido = new Pedido(mesero, listo);
        when(pedidoDAO.buscarPorId(10L)).thenReturn(pedido);

        assertThatThrownBy(() -> pedidoService.marcarAlertaRecibida(10L, "otro@restaurante.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}