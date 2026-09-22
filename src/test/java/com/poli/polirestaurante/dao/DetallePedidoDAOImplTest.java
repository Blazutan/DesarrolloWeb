package com.poli.polirestaurante.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.repository.DetallePedidoRepository;

@ExtendWith(MockitoExtension.class)
class DetallePedidoDAOImplTest {

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @InjectMocks
    private DetallePedidoDAOImpl detallePedidoDAO;

    private DetallePedido detallePedido;

    @BeforeEach
    void setUp() {
        Mesero mesero = new Mesero("Carlos Ruiz", "carlos@polirestaurante.com", "hashedpassword");
        mesero.setId(1L);
        EstadoPedido estado = new EstadoPedido("PENDIENTE");
        estado.setId(1L);
        Pedido pedido = new Pedido(mesero, estado);
        pedido.setId(1L);

        Categoria categoria = new Categoria("Bebidas");
        categoria.setId(1L);
        Producto producto = new Producto("Limonada", "Limonada natural", new BigDecimal("6000.00"), true, categoria);
        producto.setId(1L);

        detallePedido = new DetallePedido(pedido, producto, 2, "sin azúcar");
        detallePedido.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarElDetalle() {
        when(detallePedidoRepository.save(detallePedido)).thenReturn(detallePedido);

        assertThat(detallePedidoDAO.crear(detallePedido)).isEqualTo(detallePedido);
        verify(detallePedidoRepository, times(1)).save(detallePedido);
    }

    @Test
    void listar_deberiaRetornarTodosLosDetalles() {
        when(detallePedidoRepository.findAll()).thenReturn(List.of(detallePedido));

        assertThat(detallePedidoDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarElDetalle() {
        when(detallePedidoRepository.findById(1L)).thenReturn(Optional.of(detallePedido));

        assertThat(detallePedidoDAO.buscarPorId(1L)).isEqualTo(detallePedido);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(detallePedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(detallePedidoDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(detallePedidoRepository.existsById(1L)).thenReturn(true);

        assertThat(detallePedidoDAO.eliminar(1L)).isTrue();
        verify(detallePedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(detallePedidoRepository.existsById(99L)).thenReturn(false);

        assertThat(detallePedidoDAO.eliminar(99L)).isFalse();
        verify(detallePedidoRepository, never()).deleteById(anyLong());
    }
}