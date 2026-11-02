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
import com.poli.polirestaurante.repository.EstadoPedidoRepository;

@ExtendWith(MockitoExtension.class)
class EstadoPedidoDAOImplTest {

    @Mock
    private EstadoPedidoRepository estadoPedidoRepository;

    @InjectMocks
    private EstadoPedidoDAOImpl estadoPedidoDAO;

    private EstadoPedido estadoPedido;

    @BeforeEach
    void setUp() {
        estadoPedido = new EstadoPedido("PENDIENTE");
        estadoPedido.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarElEstado() {
        when(estadoPedidoRepository.save(estadoPedido)).thenReturn(estadoPedido);

        EstadoPedido resultado = estadoPedidoDAO.crear(estadoPedido);

        assertThat(resultado).isEqualTo(estadoPedido);
        verify(estadoPedidoRepository, times(1)).save(estadoPedido);
    }

    @Test
    void listar_deberiaRetornarTodosLosEstados() {
        when(estadoPedidoRepository.findAll()).thenReturn(List.of(estadoPedido, new EstadoPedido("LISTO")));

        List<EstadoPedido> resultado = estadoPedidoDAO.listar();

        assertThat(resultado).hasSize(2);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarElEstado() {
        when(estadoPedidoRepository.findById(1L)).thenReturn(Optional.of(estadoPedido));

        assertThat(estadoPedidoDAO.buscarPorId(1L)).isEqualTo(estadoPedido);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(estadoPedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(estadoPedidoDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(estadoPedidoRepository.existsById(1L)).thenReturn(true);

        assertThat(estadoPedidoDAO.eliminar(1L)).isTrue();
        verify(estadoPedidoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(estadoPedidoRepository.existsById(99L)).thenReturn(false);

        assertThat(estadoPedidoDAO.eliminar(99L)).isFalse();
        verify(estadoPedidoRepository, never()).deleteById(anyLong());
    }
}