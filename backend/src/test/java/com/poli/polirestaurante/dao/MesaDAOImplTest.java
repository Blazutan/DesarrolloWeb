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

import com.poli.polirestaurante.modelo.Mesa;
import com.poli.polirestaurante.repository.MesaRepository;

@ExtendWith(MockitoExtension.class)
class MesaDAOImplTest {

    @Mock
    private MesaRepository mesaRepository;

    @InjectMocks
    private MesaDAOImpl mesaDAO;

    private Mesa mesa;

    @BeforeEach
    void setUp() {
        mesa = new Mesa(5, "LIBRE");
        mesa.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarLaMesa() {
        when(mesaRepository.save(mesa)).thenReturn(mesa);

        assertThat(mesaDAO.crear(mesa)).isEqualTo(mesa);
        verify(mesaRepository, times(1)).save(mesa);
    }

    @Test
    void listar_deberiaRetornarTodasLasMesas() {
        when(mesaRepository.findAll()).thenReturn(List.of(mesa));

        assertThat(mesaDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarLaMesa() {
        when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesa));

        assertThat(mesaDAO.buscarPorId(1L)).isEqualTo(mesa);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(mesaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(mesaDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(mesaRepository.existsById(1L)).thenReturn(true);

        assertThat(mesaDAO.eliminar(1L)).isTrue();
        verify(mesaRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(mesaRepository.existsById(99L)).thenReturn(false);

        assertThat(mesaDAO.eliminar(99L)).isFalse();
        verify(mesaRepository, never()).deleteById(anyLong());
    }
}