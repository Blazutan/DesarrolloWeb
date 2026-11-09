package com.poli.polirestaurante.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI restauranteOpenApi() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("PoliRestaurante API")
                        .version("1.0.0")
                        .description("API REST para menú, pedidos, cocina y administración. "
                                + "Las rutas protegidas usan HTTP Basic con el correo y la contraseña de la cuenta."))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }

    @Bean
    OpenApiCustomizer roleRequirements() {
        return api -> api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
            List<String> roles = rolesFor(path, method);
            if (roles != null) {
                operation.addExtension("x-required-roles", roles);
            }
        }));
    }

    private List<String> rolesFor(String path, HttpMethod method) {
        String verb = method.name();
        if ("/api/auth/me".equals(path)) {
            return List.of("MESERO", "COCINA", "ADMINISTRADOR");
        }
        if (path.startsWith("/api/administracion/")) {
            return List.of("ADMINISTRADOR");
        }
        if ("/api/mesas/disponibles".equals(path)) {
            return List.of("MESERO", "ADMINISTRADOR");
        }
        if (path.startsWith("/api/pedidos")) {
            if ("/api/pedidos".equals(path)) {
                return "POST".equals(verb) ? List.of("MESERO") : List.of("COCINA", "ADMINISTRADOR");
            }
            if (path.endsWith("/cancelacion-cocina")) {
                return List.of("COCINA");
            }
            if ("/api/pedidos/mis-pedidos".equals(path) || "/api/pedidos/alertas".equals(path)
                    || path.endsWith("/entrega") || path.endsWith("/recibido") || path.endsWith("/cancelacion")) {
                return List.of("MESERO");
            }
            if (path.endsWith("/cola/cocina") || path.endsWith("/estado")) {
                return List.of("COCINA");
            }
            return "GET".equals(verb) ? List.of("COCINA", "ADMINISTRADOR") : List.of("ADMINISTRADOR");
        }
        if (path.startsWith("/api/detalles-pedido")) {
            return switch (verb) {
                case "GET" -> List.of("MESERO", "COCINA", "ADMINISTRADOR");
                case "POST" -> List.of("MESERO");
                default -> List.of("MESERO", "ADMINISTRADOR");
            };
        }
        if (path.startsWith("/api/estados-pedido")) {
            return "GET".equals(verb) ? List.of("COCINA", "ADMINISTRADOR") : List.of("ADMINISTRADOR");
        }
        if (path.startsWith("/api/productos") || path.startsWith("/api/categorias")) {
            return "GET".equals(verb) ? List.of("MESERO", "COCINA", "ADMINISTRADOR") : List.of("ADMINISTRADOR");
        }
        return null;
    }
}
