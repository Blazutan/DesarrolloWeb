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

import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.repository.MeseroRepository;

@ExtendWith(MockitoExtension.class)
class MeseroDAOImplTest {

    @Mock
    private MeseroRepository meseroRepository;

    @InjectMocks
    private MeseroDAOImpl meseroDAO;

    private Mesero mesero;

    @BeforeEach
    void setUp() {
        mesero = new Mesero("niño", "niño@polirestaurante.com", "hashedpassword");
        mesero.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarElMesero() {
        when(meseroRepository.save(mesero)).thenReturn(mesero);

        assertThat(meseroDAO.crear(mesero)).isEqualTo(mesero);
        verify(meseroRepository, times(1)).save(mesero);
    }

    @Test
    void listar_deberiaRetornarTodosLosMeseros() {
        when(meseroRepository.findAll()).thenReturn(List.of(mesero));

        assertThat(meseroDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarElMesero() {
        when(meseroRepository.findById(1L)).thenReturn(Optional.of(mesero));

        assertThat(meseroDAO.buscarPorId(1L)).isEqualTo(mesero);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(meseroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(meseroDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(meseroRepository.existsById(1L)).thenReturn(true);

        assertThat(meseroDAO.eliminar(1L)).isTrue();
        verify(meseroRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(meseroRepository.existsById(99L)).thenReturn(false);

        assertThat(meseroDAO.eliminar(99L)).isFalse();
        verify(meseroRepository, never()).deleteById(anyLong());
    }
}