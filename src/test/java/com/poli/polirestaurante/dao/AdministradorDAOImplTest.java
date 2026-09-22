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

import com.poli.polirestaurante.modelo.Administrador;
import com.poli.polirestaurante.repository.AdministradorRepository;

@ExtendWith(MockitoExtension.class)
class AdministradorDAOImplTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @InjectMocks
    private AdministradorDAOImpl administradorDAO;

    private Administrador administrador;

    @BeforeEach
    void setUp() {
        administrador = new Administrador("Soto", "soto@polirestaurante.com", "hashedpassword");
        administrador.setId(1L);
    }

    @Test
    void crear_deberiaGuardarYRetornarElAdministrador() {
        when(administradorRepository.save(administrador)).thenReturn(administrador);

        assertThat(administradorDAO.crear(administrador)).isEqualTo(administrador);
        verify(administradorRepository, times(1)).save(administrador);
    }

    @Test
    void listar_deberiaRetornarTodosLosAdministradores() {
        when(administradorRepository.findAll()).thenReturn(List.of(administrador));

        assertThat(administradorDAO.listar()).hasSize(1);
    }

    @Test
    void buscarPorId_cuandoExiste_deberiaRetornarElAdministrador() {
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(administrador));

        assertThat(administradorDAO.buscarPorId(1L)).isEqualTo(administrador);
    }

    @Test
    void buscarPorId_cuandoNoExiste_deberiaRetornarNull() {
        when(administradorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(administradorDAO.buscarPorId(99L)).isNull();
    }

    @Test
    void eliminar_cuandoExiste_deberiaRetornarTrue() {
        when(administradorRepository.existsById(1L)).thenReturn(true);

        assertThat(administradorDAO.eliminar(1L)).isTrue();
        verify(administradorRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminar_cuandoNoExiste_deberiaRetornarFalse() {
        when(administradorRepository.existsById(99L)).thenReturn(false);

        assertThat(administradorDAO.eliminar(99L)).isFalse();
        verify(administradorRepository, never()).deleteById(anyLong());
    }
}