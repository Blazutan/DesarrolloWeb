package com.poli.polirestaurante.dao;

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

import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoDAOImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoDAOImpl pedidoDAO;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        Mesero mesero = new Mesero("Carlos Ruiz", "carlos@polirestaurante.com", "hashedpassword");
        mesero.setId(1L);
        EstadoPedido estado = new EstadoPedido("PENDIENTE");
        estado.setId(1L);

        pedido = new Pedido(mesero, estado);
        pedido.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarElPedido() {
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        assertThat(pedidoDAO.crear(pedido)).isEqualTo(pedido);
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    void listar_deberiaRetornarTodosLosPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

        assertThat(pedidoDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarElPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThat(pedidoDAO.buscarPorId(1L)).isEqualTo(pedido);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(pedidoDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(pedidoRepository.existsById(1L)).thenReturn(true);

        assertThat(pedidoDAO.eliminar(1L)).isTrue();
        verify(pedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(pedidoRepository.existsById(99L)).thenReturn(false);

        assertThat(pedidoDAO.eliminar(99L)).isFalse();
        verify(pedidoRepository, never()).deleteById(anyLong());
    }
}