package com.poli.polirestaurante.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poli.polirestaurante.dao.AdministradorDAO;
import com.poli.polirestaurante.dao.CocinaDAO;
import com.poli.polirestaurante.dao.MesaDAO;
import com.poli.polirestaurante.dao.MeseroDAO;
import com.poli.polirestaurante.dto.UsuarioRequest;
import com.poli.polirestaurante.modelo.Mesa;
import com.poli.polirestaurante.modelo.Mesero;

@ExtendWith(MockitoExtension.class)
class AdministracionServiceTest {

    @Mock
    private MesaDAO mesaDAO;

    @Mock
    private MeseroDAO meseroDAO;

    @Mock
    private CocinaDAO cocinaDAO;

    @Mock
    private AdministradorDAO administradorDAO;

    @InjectMocks
    private AdministracionService administracionService;

    @Test
    void actualizarEstadoMesa_aceptaEstadosOperativos() {
        Mesa mesa = new Mesa(12, "LIBRE");
        mesa.setId(3L);
        when(mesaDAO.buscarPorId(3L)).thenReturn(mesa);
        when(mesaDAO.actualizar(mesa)).thenReturn(mesa);

        var resultado = administracionService.actualizarEstadoMesa(3L, "ocupada");

        assertThat(resultado.estado()).isEqualTo("OCUPADA");
        assertThat(mesa.getEstado()).isEqualTo("OCUPADA");
    }

    @Test
    void actualizarEstadoMesa_rechazaEstadosDesconocidos() {
        when(mesaDAO.buscarPorId(3L)).thenReturn(new Mesa(12, "LIBRE"));

        assertThatThrownBy(() -> administracionService.actualizarEstadoMesa(3L, "RESERVADA"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crearUsuario_normalizaElCorreoYNoDevuelveLaContrasena() {
        when(meseroDAO.listar()).thenReturn(List.of());
        when(cocinaDAO.listar()).thenReturn(List.of());
        when(administradorDAO.listar()).thenReturn(List.of());
        when(meseroDAO.crear(any(Mesero.class))).thenAnswer(invocacion -> {
            Mesero mesero = invocacion.getArgument(0);
            mesero.setId(8L);
            return mesero;
        });

        var resultado = administracionService.crearUsuario("mesero",
                new UsuarioRequest("Ana Ruiz", "ANA@RESTAURANTE.COM", "secreto"));

        assertThat(resultado.id()).isEqualTo(8L);
        assertThat(resultado.correo()).isEqualTo("ana@restaurante.com");
        assertThat(resultado.rol()).isEqualTo("MESERO");
    }

    @Test
    void crearUsuario_rechazaCorreoYaAsignadoAOtroRol() {
        when(meseroDAO.listar()).thenReturn(List.of(new Mesero("Ana", "ana@restaurante.com", "hash")));
        when(cocinaDAO.listar()).thenReturn(List.of());
        when(administradorDAO.listar()).thenReturn(List.of());

        assertThatThrownBy(() -> administracionService.crearUsuario("COCINA",
                new UsuarioRequest("Ana", "ana@restaurante.com", "secreto")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}