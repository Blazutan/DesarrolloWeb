package com.poli.polirestaurante.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poli.polirestaurante.dao.PedidoDAO;
import com.poli.polirestaurante.dto.ReporteTiemposResponse;
import com.poli.polirestaurante.dto.ReporteVentasResponse;
import com.poli.polirestaurante.dto.ReporteVentasResponse.DetalleVendido;
import com.poli.polirestaurante.dto.ReporteVentasResponse.PedidoVendido;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.Pedido;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final PedidoDAO pedidoDAO;

    public ReporteService(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    public ReporteVentasResponse ventas(LocalDate desde, LocalDate hasta) {
        List<Pedido> entregados = entregadosEntre(desde, hasta);

        BigDecimal totalVentas = entregados.stream()
                .map(Pedido::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticketPromedio = entregados.isEmpty()
                ? BigDecimal.ZERO
                : totalVentas.divide(BigDecimal.valueOf(entregados.size()), 2, RoundingMode.HALF_UP);

        List<PedidoVendido> pedidos = entregados.stream()
                .sorted(Comparator.comparing(Pedido::getFechaEntregado).thenComparing(Pedido::getId))
                .map(pedido -> new PedidoVendido(
                        pedido.getId(),
                        pedido.getFechaEntregado(),
                        pedido.getMesas().stream().findFirst().map(mesa -> mesa.getNumero()).orElse(null),
                        pedido.getPrecioTotal(),
                        pedido.getDetalles().stream()
                                .sorted(Comparator.comparing(DetallePedido::getId,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                                .map(detalle -> new DetalleVendido(
                                        detalle.getProducto().getId(),
                                        detalle.getProducto().getNombre(),
                                        detalle.getCantidad(),
                                        detalle.getPrecioUnitario(),
                                        detalle.calcularSubtotal()))
                                .toList()))
                .toList();

        return new ReporteVentasResponse(desde, hasta, entregados.size(), totalVentas, ticketPromedio, pedidos);
    }

    public ReporteTiemposResponse tiempos(LocalDate desde, LocalDate hasta) {
        List<Pedido> entregados = entregadosEntre(desde, hasta);
        return new ReporteTiemposResponse(desde, hasta, entregados.size(),
                promedioSegundos(entregados, Pedido::getFechaEnPreparacion, Pedido::getFechaListo),
                promedioSegundos(entregados, Pedido::getFechaListo, Pedido::getFechaEntregado),
                promedioSegundos(entregados, Pedido::getFechaEnPreparacion, Pedido::getFechaEntregado));
    }

    private List<Pedido> entregadosEntre(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El rango de fechas no es válido.");
        }
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay();
        return pedidoDAO.listar().stream()
                .filter(pedido -> pedido.getFechaEntregado() != null)
                .filter(pedido -> !pedido.getFechaEntregado().isBefore(inicio)
                        && pedido.getFechaEntregado().isBefore(fin))
                .toList();
    }

    private Long promedioSegundos(List<Pedido> pedidos, Function<Pedido, LocalDateTime> inicio,
            Function<Pedido, LocalDateTime> fin) {
        OptionalDouble promedio = pedidos.stream()
                .filter(pedido -> inicio.apply(pedido) != null && fin.apply(pedido) != null)
                .mapToLong(pedido -> Duration.between(inicio.apply(pedido), fin.apply(pedido)).getSeconds())
                .average();
        return promedio.isPresent() ? Math.round(promedio.getAsDouble()) : null;
    }
}