package com.poli.polirestaurante.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.modelo.Cocina;
import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.modelo.Mesa;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.repository.CategoriaRepository;
import com.poli.polirestaurante.repository.CocinaRepository;
import com.poli.polirestaurante.repository.DetallePedidoRepository;
import com.poli.polirestaurante.repository.EstadoPedidoRepository;
import com.poli.polirestaurante.repository.MesaRepository;
import com.poli.polirestaurante.repository.MeseroRepository;
import com.poli.polirestaurante.repository.PedidoRepository;
import com.poli.polirestaurante.repository.ProductoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoFlowIntegrationTest {

    private static final String PASSWORD = "test-password-only";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CocinaRepository cocinaRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private EstadoPedidoRepository estadoPedidoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private MeseroRepository meseroRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private String correoMesero;
    private String correoCocina;
    private Long mesaId;
    private Long productoId;
    private int numeroMesa;

    @BeforeEach
    void prepararDatosAislados() {
        String sufijo = UUID.randomUUID().toString();
        correoMesero = "mesero-" + sufijo + "@example.test";
        correoCocina = "cocina-" + sufijo + "@example.test";

        meseroRepository.save(new Mesero("Mesero de prueba", correoMesero, passwordEncoder.encode(PASSWORD)));
        cocinaRepository.save(new Cocina("Cocina de prueba", correoCocina, passwordEncoder.encode(PASSWORD)));
        estadoPedidoRepository.saveAll(List.of(
                new EstadoPedido("PENDIENTE"),
                new EstadoPedido("EN PREPARACION"),
                new EstadoPedido("LISTO"),
                new EstadoPedido("ENTREGADO"),
                new EstadoPedido("CANCELADO")));

        Categoria categoria = categoriaRepository.save(new Categoria("Prueba " + sufijo));
        Producto producto = productoRepository.save(new Producto("Producto " + sufijo, "Dato de prueba",
                new BigDecimal("12500.00"), true, categoria));
        numeroMesa = Math.floorMod(sufijo.hashCode(), 1_000_000) + 1;
        Mesa mesa = mesaRepository.save(new Mesa(numeroMesa, "LIBRE"));
        productoId = producto.getId();
        mesaId = mesa.getId();
    }

    @Test
    void meseroCreaPedidoCocinaLoPreparaYLaEntregaLiberaLaMesa() throws Exception {
        String solicitud = """
                {"mesaId":%d,"detalles":[{"productoId":%d,"cantidad":2,"notas":"sin sal"}]}
                """.formatted(mesaId, productoId);

        mockMvc.perform(post("/api/pedidos").with(httpBasic(correoMesero, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(solicitud))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.mesaId").value(mesaId))
                .andExpect(jsonPath("$.mesaNumero").value(numeroMesa));

        var pedido = pedidoRepository.findAll().getFirst();
        assertThat(pedido.getPrecioTotal()).isEqualByComparingTo("25000.00");
        assertThat(detallePedidoRepository.findAll()).hasSize(1);
        assertThat(mesaRepository.findById(mesaId).orElseThrow().getEstado()).isEqualTo("OCUPADA");

        mockMvc.perform(get("/api/pedidos/mis-pedidos").with(httpBasic(correoMesero, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(pedido.getId()));
        mockMvc.perform(get("/api/pedidos/mis-pedidos").with(httpBasic(correoCocina, PASSWORD)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/pedidos/" + pedido.getId() + "/estado")
                        .param("estado", "EN PREPARACIÓN").with(httpBasic(correoCocina, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN PREPARACION"));
        mockMvc.perform(patch("/api/pedidos/" + pedido.getId() + "/estado")
                        .param("estado", "LISTO").with(httpBasic(correoCocina, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("LISTO"));
        mockMvc.perform(patch("/api/pedidos/" + pedido.getId() + "/entrega")
                        .with(httpBasic(correoMesero, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));

        assertThat(pedidoRepository.findById(pedido.getId()).orElseThrow().getFechaEntregado()).isNotNull();
        Mesa mesaLiberada = mesaRepository.findById(mesaId).orElseThrow();
        assertThat(mesaLiberada.getEstado()).isEqualTo("LIBRE");
        assertThat(mesaLiberada.getPedido()).isNull();
    }

    @Test
    void apiRechazaCreacionDePedidoSinDetalles() throws Exception {
        String solicitud = "{\"mesaId\":" + mesaId + ",\"detalles\":[]}";
        mockMvc.perform(post("/api/pedidos").with(httpBasic(correoMesero, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(solicitud))
                .andExpect(status().isBadRequest());
        assertThat(pedidoRepository.findAll()).isEmpty();
    }

    @Test
    void cocinaCancelaPedidoYLaMesaSeLibera() throws Exception {
        String solicitud = """
                {"mesaId":%d,"detalles":[{"productoId":%d,"cantidad":1,"notas":""}]}
                """.formatted(mesaId, productoId);
        mockMvc.perform(post("/api/pedidos").with(httpBasic(correoMesero, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON).content(solicitud))
                .andExpect(status().isCreated());

        var pedido = pedidoRepository.findAll().getFirst();
        mockMvc.perform(patch("/api/pedidos/" + pedido.getId() + "/cancelacion-cocina")
                        .with(httpBasic(correoCocina, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Producto no disponible\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoCancelacion").value("Producto no disponible"));

        assertThat(pedidoRepository.findById(pedido.getId()).orElseThrow()
                .getMotivoCancelacion()).isEqualTo("Producto no disponible");
        Mesa mesaLiberada = mesaRepository.findById(mesaId).orElseThrow();
        assertThat(mesaLiberada.getEstado()).isEqualTo("LIBRE");
        assertThat(mesaLiberada.getPedido()).isNull();
    }
}
