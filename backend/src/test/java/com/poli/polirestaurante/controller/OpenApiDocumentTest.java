package com.poli.polirestaurante.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentoPublicaOperacionesContratosYAutenticacionBasic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.paths['/api/auth/me'].get").exists())
                .andExpect(jsonPath("$.paths['/api/pedidos'].post").exists())
                .andExpect(jsonPath("$.paths['/api/pedidos/mis-pedidos'].get").exists())
                .andExpect(jsonPath("$.paths['/api/mesas/disponibles'].get").exists())
                .andExpect(jsonPath("$.paths['/api/pedidos'].post['x-required-roles'][0]").value("MESERO"))
                .andExpect(jsonPath("$.paths['/api/pedidos'].get['x-required-roles'][0]").value("COCINA"))
                .andExpect(jsonPath("$.paths['/api/pedidos/{id}/cancelacion-cocina'].patch['x-required-roles'][0]").value("COCINA"))
                .andExpect(jsonPath("$.components.schemas.PedidoRequest.properties.detalles").exists());
    }
}
