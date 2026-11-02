package com.poli.polirestaurante.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.poli.polirestaurante.dao.EstadoPedidoDAO;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.security.SecurityConfig;

@WebMvcTest(EstadoPedidoController.class)
@Import(SecurityConfig.class)
class EstadoPedidoRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstadoPedidoDAO estadoPedidoDAO;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crudEstadoPedido_exponePostGetPutYDelete() throws Exception {
        EstadoPedido estado = new EstadoPedido("LISTO");
        estado.setId(4L);
        when(estadoPedidoDAO.crear(any(EstadoPedido.class))).thenReturn(estado);
        when(estadoPedidoDAO.listar()).thenReturn(List.of(estado));
        when(estadoPedidoDAO.buscarPorId(4L)).thenReturn(estado);
        when(estadoPedidoDAO.actualizar(estado)).thenReturn(estado);
        when(estadoPedidoDAO.eliminar(4L)).thenReturn(true);

        mockMvc.perform(post("/api/estados-pedido").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"LISTO\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/estados-pedido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("LISTO"));
        mockMvc.perform(get("/api/estados-pedido/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
        mockMvc.perform(put("/api/estados-pedido/4").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"ENTREGADO\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/estados-pedido/4").with(csrf()))
                .andExpect(status().isNoContent());
    }
}