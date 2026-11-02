package com.poli.polirestaurante.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.dto.ReporteTiemposResponse;
import com.poli.polirestaurante.dto.ReporteVentasResponse;
import com.poli.polirestaurante.dto.ReporteVentasResponse.ProductoVendido;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.modelo.Producto;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 10, 31);

    @Mock
    private PedidoDAO pedidoDAO;

    @InjectMocks
    private ReporteService reporteService;

    private Producto producto(String nombre, String precio) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        return producto;
    }

    private Pedido pedido(LocalDateTime entregado) {
        Pedido pedido = new Pedido();
        pedido.setFechaEntregado(entregado);
        return pedido;
    }

    private void agregar(Pedido pedido, Producto producto, int cantidad) {
        pedido.agregarDetalle(new DetallePedido(pedido, producto, cantidad, null));
    }

    private Pedido pedidoConTiempos(LocalDateTime enPreparacion, LocalDateTime listo, LocalDateTime entregado) {
        Pedido pedido = pedido(entregado);
        pedido.setFechaEnPreparacion(enPreparacion);
        pedido.setFechaListo(listo);
        return pedido;
    }

    @Test
    void ventas_sumaTotalesYAgrupaProductosOrdenadosPorUnidades() {
        Producto limonada = producto("Limonada", "6000");
        Producto hamburguesa = producto("Hamburguesa", "15000");

        Pedido p1 = pedido(LocalDateTime.of(2026, 10, 10, 12, 0));
        agregar(p1, limonada, 2);
        Pedido p2 = pedido(LocalDateTime.of(2026, 10, 11, 13, 0));
        agregar(p2, limonada, 1);
        agregar(p2, hamburguesa, 1);
        Pedido sinEntregar = pedido(null);
        agregar(sinEntregar, hamburguesa, 5);
        Pedido fueraDeRango = pedido(LocalDateTime.of(2026, 11, 5, 12, 0));
        agregar(fueraDeRango, hamburguesa, 5);

        when(pedidoDAO.listar()).thenReturn(List.of(p1, p2, sinEntregar, fueraDeRango));

        ReporteVentasResponse resultado = reporteService.ventas(DESDE, HASTA);

        assertThat(resultado.totalPedidos()).isEqualTo(2);
        assertThat(resultado.totalVentas()).isEqualByComparingTo("33000");
        assertThat(resultado.ticketPromedio()).isEqualByComparingTo("16500.00");
        assertThat(resultado.productos())
                .extracting(ProductoVendido::producto)
                .containsExactly("Limonada", "Hamburguesa");
        assertThat(resultado.productos().get(0).unidades()).isEqualTo(3L);
        assertThat(resultado.productos().get(0).total()).isEqualByComparingTo("18000");
    }

    @Test
    void ventas_incluyeElUltimoDiaDelRangoYExcluyeElSiguiente() {
        Producto limonada = producto("Limonada", "6000");
        Pedido ultimoDia = pedido(LocalDateTime.of(2026, 10, 31, 23, 59));
        agregar(ultimoDia, limonada, 1);
        Pedido diaSiguiente = pedido(LocalDateTime.of(2026, 11, 1, 0, 0));
        agregar(diaSiguiente, limonada, 1);

        when(pedidoDAO.listar()).thenReturn(List.of(ultimoDia, diaSiguiente));

        ReporteVentasResponse resultado = reporteService.ventas(DESDE, HASTA);

        assertThat(resultado.totalPedidos()).isEqualTo(1);
        assertThat(resultado.totalVentas()).isEqualByComparingTo("6000");
    }

    @Test
    void ventas_sinPedidos_devuelveCeros() {
        when(pedidoDAO.listar()).thenReturn(List.of());

        ReporteVentasResponse resultado = reporteService.ventas(DESDE, HASTA);

        assertThat(resultado.totalPedidos()).isZero();
        assertThat(resultado.totalVentas()).isEqualByComparingTo("0");
        assertThat(resultado.ticketPromedio()).isEqualByComparingTo("0");
        assertThat(resultado.productos()).isEmpty();
    }

    @Test
    void ventas_conRangoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> reporteService.ventas(HASTA, DESDE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rango de fechas");
    }

    @Test
    void tiempos_calculaLosTresPromediosEnSegundos() {
        Pedido p1 = pedidoConTiempos(
                LocalDateTime.of(2026, 10, 10, 10, 0),
                LocalDateTime.of(2026, 10, 10, 10, 10),
                LocalDateTime.of(2026, 10, 10, 10, 12));
        Pedido p2 = pedidoConTiempos(
                LocalDateTime.of(2026, 10, 11, 11, 0),
                LocalDateTime.of(2026, 10, 11, 11, 20),
                LocalDateTime.of(2026, 10, 11, 11, 24));

        when(pedidoDAO.listar()).thenReturn(List.of(p1, p2));

        ReporteTiemposResponse resultado = reporteService.tiempos(DESDE, HASTA);

        assertThat(resultado.pedidosAnalizados()).isEqualTo(2);
        assertThat(resultado.promedioPreparacionSegundos()).isEqualTo(900L);
        assertThat(resultado.promedioEsperaEntregaSegundos()).isEqualTo(180L);
        assertThat(resultado.promedioTotalSegundos()).isEqualTo(1080L);
    }

    @Test
    void tiempos_sinPedidos_devuelvePromediosNulos() {
        when(pedidoDAO.listar()).thenReturn(List.of());

        ReporteTiemposResponse resultado = reporteService.tiempos(DESDE, HASTA);

        assertThat(resultado.pedidosAnalizados()).isZero();
        assertThat(resultado.promedioPreparacionSegundos()).isNull();
        assertThat(resultado.promedioEsperaEntregaSegundos()).isNull();
        assertThat(resultado.promedioTotalSegundos()).isNull();
    }
}