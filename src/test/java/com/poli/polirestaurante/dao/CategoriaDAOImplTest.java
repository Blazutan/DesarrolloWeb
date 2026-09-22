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

import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaDAOImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaDAOImpl categoriaDAO;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Bebidas");
        categoria.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarLaCategoria() {
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaDAO.crear(categoria);

        assertThat(resultado).isEqualTo(categoria);
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void listar_deberiaRetornarTodasLasCategorias() {
        List<Categoria> categorias = List.of(categoria, new Categoria("Postres"));
        when(categoriaRepository.findAll()).thenReturn(categorias);

        List<Categoria> resultado = categoriaDAO.listar();

        assertThat(resultado).hasSize(2);
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarLaCategoria() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaDAO.buscarPorId(1L);

        assertThat(resultado).isEqualTo(categoria);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        Categoria resultado = categoriaDAO.buscarPorId(99L);

        assertThat(resultado).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaEliminarYRetornarTrue() {
        when(categoriaRepository.existsById(1L)).thenReturn(true);

        boolean resultado = categoriaDAO.eliminar(1L);

        assertThat(resultado).isTrue();
        verify(categoriaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalseSinEliminar() {
        when(categoriaRepository.existsById(99L)).thenReturn(false);

        boolean resultado = categoriaDAO.eliminar(99L);

        assertThat(resultado).isFalse();
        verify(categoriaRepository, never()).deleteById(anyLong());
    }
}