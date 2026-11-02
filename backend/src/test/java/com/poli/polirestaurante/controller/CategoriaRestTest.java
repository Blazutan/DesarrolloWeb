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

import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.security.SecurityConfig;
import com.poli.polirestaurante.service.CategoriaService;

@WebMvcTest(CategoriaController.class)
@Import(SecurityConfig.class)
class CategoriaRestTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void crudCategoria_exponePostGetPutYDelete() throws Exception {
        Categoria categoria = new Categoria("Bebidas");
        categoria.setId(7L);
        when(categoriaService.crear(any(Categoria.class))).thenReturn(categoria);
        when(categoriaService.listar()).thenReturn(List.of(categoria));
        when(categoriaService.buscarPorId(7L)).thenReturn(categoria);
        when(categoriaService.actualizar(org.mockito.ArgumentMatchers.eq(7L), any(Categoria.class)))
                .thenReturn(categoria);
        when(categoriaService.eliminar(7L)).thenReturn(true);

        mockMvc.perform(post("/api/categorias").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Bebidas"));
        mockMvc.perform(get("/api/categorias/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
        mockMvc.perform(put("/api/categorias/7").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/categorias/7").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "MESERO")
    void escrituraCategoria_rechazaMesero() throws Exception {
        mockMvc.perform(post("/api/categorias").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isForbidden());
    }
}