package com.poli.polirestaurante.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.security.SecurityConfig;
import com.poli.polirestaurante.service.PedidoService;

@WebMvcTest(PedidoController.class)
@Import(SecurityConfig.class)
class PedidoCrudRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void pedido_exponeGetColeccionGetIdPutYDelete() throws Exception {
        Mesero mesero = new Mesero("Ana", "ana@restaurante.com", "hash");
        mesero.setId(3L);
        Pedido pedido = new Pedido(mesero, new EstadoPedido("PENDIENTE"));
        pedido.setId(5L);
        pedido.setFechaUltimaActualizacion(LocalDateTime.now());
        when(pedidoService.listar()).thenReturn(List.of(pedido));
        when(pedidoService.buscarPorId(5L)).thenReturn(pedido);
        when(pedidoService.actualizarAdministrativo(5L, 3L, null)).thenReturn(pedido);
        when(pedidoService.eliminar(5L)).thenReturn(true);

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].meseroId").value(3));
        mockMvc.perform(get("/api/pedidos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mockMvc.perform(put("/api/pedidos/5").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"meseroId\":3}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/pedidos/5").with(csrf()))
                .andExpect(status().isNoContent());
    }
}