package com.poli.polirestaurante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poli.polirestaurante.dao.CategoriaDAO;
import com.poli.polirestaurante.dao.ProductoDAO;
import com.poli.polirestaurante.dto.ProductoRequest;
import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.modelo.Producto;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private CategoriaDAO categoriaDAO;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void crear_asociaCategoriaYConservaPrecio() {
        Categoria categoria = new Categoria("Bebidas");
        categoria.setId(2L);
        when(categoriaDAO.buscarPorId(2L)).thenReturn(categoria);
        when(productoDAO.crear(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto producto = productoService.crear(
                new ProductoRequest(" Café ", "Tinto", new BigDecimal("5000"), true, 2L));

        assertThat(producto.getNombre()).isEqualTo("Café");
        assertThat(producto.getCategoria()).isSameAs(categoria);
        assertThat(producto.getPrecio()).isEqualByComparingTo("5000");
    }

    @Test
    void actualizar_actualizaPrecioConLaOperacionDelModelo() {
        Categoria categoria = new Categoria("Bebidas");
        Producto producto = new Producto("Café", "Tinto", new BigDecimal("5000"), true, categoria);
        when(productoDAO.buscarPorId(9L)).thenReturn(producto);
        when(categoriaDAO.buscarPorId(2L)).thenReturn(categoria);
        when(productoDAO.actualizar(producto)).thenReturn(producto);

        Producto actualizado = productoService.actualizar(9L,
                new ProductoRequest("Café", "Tinto", new BigDecimal("6000"), true, 2L));

        assertThat(actualizado.getPrecio()).isEqualByComparingTo("6000");
    }
}