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

import com.poli.polirestaurante.modelo.Cocina;
import com.poli.polirestaurante.repository.CocinaRepository;

@ExtendWith(MockitoExtension.class)
class CocinaDAOImplTest {

    @Mock
    private CocinaRepository cocinaRepository;

    @InjectMocks
    private CocinaDAOImpl cocinaDAO;

    private Cocina cocina;

    @BeforeEach
    void setUp() {
        cocina = new Cocina("Charlie", "charlie@polirestaurante.com", "hashedpassword");
        cocina.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarLaCocina() {
        when(cocinaRepository.save(cocina)).thenReturn(cocina);

        assertThat(cocinaDAO.crear(cocina)).isEqualTo(cocina);
        verify(cocinaRepository, times(1)).save(cocina);
    }

    @Test
    void listar_deberiaRetornarTodosLasCocinas() {
        when(cocinaRepository.findAll()).thenReturn(List.of(cocina));

        assertThat(cocinaDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarLaCocina() {
        when(cocinaRepository.findById(1L)).thenReturn(Optional.of(cocina));

        assertThat(cocinaDAO.buscarPorId(1L)).isEqualTo(cocina);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(cocinaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(cocinaDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(cocinaRepository.existsById(1L)).thenReturn(true);

        assertThat(cocinaDAO.eliminar(1L)).isTrue();
        verify(cocinaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(cocinaRepository.existsById(99L)).thenReturn(false);

        assertThat(cocinaDAO.eliminar(99L)).isFalse();
        verify(cocinaRepository, never()).deleteById(anyLong());
    }
}