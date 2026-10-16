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

import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.security.SecurityConfig;
import com.poli.polirestaurante.service.ProductoService;

@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crudProducto_exponePostGetPutYDelete() throws Exception {
        Categoria categoria = new Categoria("Bebidas");
        categoria.setId(2L);
        Producto producto = new Producto("Café", "Tinto", new BigDecimal("5000"), true, categoria);
        producto.setId(9L);
        when(productoService.crear(any())).thenReturn(producto);
        when(productoService.listar()).thenReturn(List.of(producto));
        when(productoService.buscarPorId(9L)).thenReturn(producto);
        when(productoService.actualizar(org.mockito.ArgumentMatchers.eq(9L), any())).thenReturn(producto);
        when(productoService.eliminar(9L)).thenReturn(true);

        String request = """
                {"nombre":"Café","descripcion":"Tinto","precio":5000,"disponibilidad":true,"categoriaId":2}
                """;

        mockMvc.perform(post("/api/productos").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precio").value(5000));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoriaNombre").value("Bebidas"));
        mockMvc.perform(get("/api/productos/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));
        mockMvc.perform(put("/api/productos/9").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/productos/9").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void escrituraDeProducto_rechazaMesero() throws Exception {
        mockMvc.perform(post("/api/productos").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}