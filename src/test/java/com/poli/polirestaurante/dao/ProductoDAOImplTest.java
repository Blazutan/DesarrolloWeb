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
import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoDAOImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoDAOImpl productoDAO;

    private Producto producto;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria("Bebidas");
        categoria.setId(1L);
        producto = new Producto("Limonada", "Limonada natural", new BigDecimal("6000.00"), true, categoria);
        producto.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarElProducto() {
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoDAO.crear(producto);

        assertThat(resultado).isEqualTo(producto);
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void listar_deberiaRetornarTodosLosProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        assertThat(productoDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarElProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThat(productoDAO.buscarPorId(1L)).isEqualTo(producto);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(productoDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(productoRepository.existsById(1L)).thenReturn(true);

        assertThat(productoDAO.eliminar(1L)).isTrue();
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(productoRepository.existsById(99L)).thenReturn(false);

        assertThat(productoDAO.eliminar(99L)).isFalse();
        verify(productoRepository, never()).deleteById(anyLong());
    }
}