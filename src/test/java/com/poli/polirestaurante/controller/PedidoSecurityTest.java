package com.poli.polirestaurante.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.poli.polirestaurante.security.SecurityConfig;
import com.poli.polirestaurante.dto.MesaResponse;
import com.poli.polirestaurante.dto.UsuarioRequest;
import com.poli.polirestaurante.dto.UsuarioResponse;
import com.poli.polirestaurante.service.AdministracionService;
import com.poli.polirestaurante.service.PedidoService;

@WebMvcTest({ PedidoController.class, AdministracionController.class })
@Import(SecurityConfig.class)
class PedidoSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private AdministracionService administracionService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void cola_sinAutenticar_requiereCredenciales() throws Exception {
        mockMvc.perform(get("/api/pedidos/cola/cocina"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void cola_rechazaMesero() throws Exception {
        mockMvc.perform(get("/api/pedidos/cola/cocina"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COCINA")
    void cola_permiteCocina() throws Exception {
        when(pedidoService.listarPendientes()).thenReturn(List.of());

        mockMvc.perform(get("/api/pedidos/cola/cocina"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "COCINA")
    void estado_permiteCocina() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/estado").param("estado", "LISTO").with(csrf()))
                .andExpect(status().isNotFound());
        verify(pedidoService).cambiarEstadoCocina(1L, "LISTO");
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void crear_permiteMesero() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(csrf()))
                .andExpect(status().isNotFound());
        verify(pedidoService).crear("user");
    }

    @Test
    @WithMockUser(roles = "COCINA")
    void crear_rechazaCocina() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COCINA")
    void entrega_rechazaCocina() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/entrega").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void entrega_permiteMesero() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/entrega").with(csrf()))
                .andExpect(status().isNotFound());
        verify(pedidoService).entregar(1L, "user");
    }

    @Test
    @WithMockUser(roles = "MESERO")
        void recibirAlerta_permiteMesero() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/recibido").with(csrf()))
                .andExpect(status().isNotFound());
        verify(pedidoService).marcarAlertaRecibida(1L, "user");
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void estado_rechazaMesero() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/estado").param("estado", "LISTO").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void administracion_rechazaMesero() throws Exception {
        mockMvc.perform(get("/api/administracion/mesas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void administracion_permiteAdministrador() throws Exception {
        when(administracionService.listarMesas()).thenReturn(List.of());

        mockMvc.perform(get("/api/administracion/mesas"))
                .andExpect(status().isOk());
    }

            @Test
            @WithMockUser(roles = "ADMINISTRADOR")
            void crudMesas_exponePostGetPutEstadoYDelete() throws Exception {
            MesaResponse mesa = new MesaResponse(2L, 12, "OCUPADA");
            when(administracionService.listarMesas()).thenReturn(List.of(mesa));
            when(administracionService.buscarMesa(2L)).thenReturn(mesa);
            when(administracionService.crearMesa(any())).thenReturn(mesa);
            when(administracionService.actualizarMesa(eq(2L), any())).thenReturn(mesa);
            when(administracionService.actualizarEstadoMesa(2L, "OCUPADA")).thenReturn(mesa);
            when(administracionService.eliminarMesa(2L)).thenReturn(true);

            mockMvc.perform(post("/api/administracion/mesas").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":12,\"estado\":\"LIBRE\"}"))
                .andExpect(status().isCreated());
            mockMvc.perform(get("/api/administracion/mesas"))
                .andExpect(status().isOk());
            mockMvc.perform(get("/api/administracion/mesas/2"))
                .andExpect(status().isOk());
            mockMvc.perform(put("/api/administracion/mesas/2").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":12,\"estado\":\"OCUPADA\"}"))
                .andExpect(status().isOk());
            mockMvc.perform(patch("/api/administracion/mesas/2/estado").param("estado", "OCUPADA").with(csrf()))
                .andExpect(status().isOk());
            mockMvc.perform(delete("/api/administracion/mesas/2").with(csrf()))
                .andExpect(status().isNoContent());
            }

            @Test
            @WithMockUser(roles = "ADMINISTRADOR")
            void crudUsuarios_exponePostGetPutYDelete() throws Exception {
            UsuarioResponse usuario = new UsuarioResponse(4L, "Ana", "ana@restaurante.com", "MESERO");
            when(administracionService.listarUsuarios()).thenReturn(List.of(usuario));
            when(administracionService.buscarUsuario("MESERO", 4L)).thenReturn(usuario);
            when(administracionService.crearUsuario(eq("MESERO"), any(UsuarioRequest.class))).thenReturn(usuario);
            when(administracionService.actualizarUsuario(eq("MESERO"), eq(4L), any(UsuarioRequest.class)))
                .thenReturn(usuario);
            when(administracionService.eliminarUsuario("MESERO", 4L)).thenReturn(true);

            String request = "{\"nombre\":\"Ana\",\"correo\":\"ana@restaurante.com\",\"password\":\"secret\"}";
            mockMvc.perform(post("/api/administracion/usuarios/MESERO").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());
            mockMvc.perform(get("/api/administracion/usuarios"))
                .andExpect(status().isOk());
            mockMvc.perform(get("/api/administracion/usuarios/MESERO/4"))
                .andExpect(status().isOk());
            mockMvc.perform(put("/api/administracion/usuarios/MESERO/4").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());
            mockMvc.perform(delete("/api/administracion/usuarios/MESERO/4").with(csrf()))
                .andExpect(status().isNoContent());
            }
}