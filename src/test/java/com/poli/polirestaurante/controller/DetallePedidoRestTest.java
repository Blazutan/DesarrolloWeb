package com.poli.polirestaurante.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.poli.polirestaurante.dto.DetallePedidoRequest;
import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.security.SecurityConfig;
import com.poli.polirestaurante.service.DetallePedidoService;

@WebMvcTest(DetallePedidoController.class)
@Import(SecurityConfig.class)
class DetallePedidoRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetallePedidoService detallePedidoService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "ana@restaurante.com", roles = "MESERO")
    void crudDetalle_exponePostGetPutYDelete() throws Exception {
        DetallePedido detalle = crearDetalle();
        when(detallePedidoService.listar("ana@restaurante.com", false)).thenReturn(List.of(detalle));
        when(detallePedidoService.buscarPorId(6L, "ana@restaurante.com", false)).thenReturn(detalle);
        when(detallePedidoService.crear(any(DetallePedidoRequest.class), eq("ana@restaurante.com")))
                .thenReturn(detalle);
        when(detallePedidoService.actualizar(eq(6L), any(DetallePedidoRequest.class),
                eq("ana@restaurante.com"), eq(false))).thenReturn(detalle);
        when(detallePedidoService.eliminar(6L, "ana@restaurante.com", false)).thenReturn(true);

        String request = "{\"pedidoId\":5,\"productoId\":8,\"cantidad\":2,\"notas\":\"Sin hielo\"}";
        mockMvc.perform(post("/api/detalles-pedido").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precioUnitario").value(2500));
        mockMvc.perform(get("/api/detalles-pedido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subtotal").value(5000));
        mockMvc.perform(get("/api/detalles-pedido/6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(5));
        mockMvc.perform(put("/api/detalles-pedido/6").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/detalles-pedido/6").with(csrf()))
                .andExpect(status().isNoContent());
    }

    private DetallePedido crearDetalle() {
        Mesero mesero = new Mesero("Ana", "ana@restaurante.com", "hash");
        mesero.setId(2L);
        Pedido pedido = new Pedido(mesero, new EstadoPedido("PENDIENTE"));
        pedido.setId(5L);
        Producto producto = new Producto("Gaseosa", "", new BigDecimal("2500"), true, null);
        producto.setId(8L);
        DetallePedido detalle = new DetallePedido(pedido, producto, 2, "Sin hielo");
        detalle.setId(6L);
        return detalle;
    }
}