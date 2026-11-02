package com.poli.polirestaurante.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole("MESERO")
                    .requestMatchers(HttpMethod.GET, "/api/pedidos/cola/cocina").hasRole("COCINA")
                    .requestMatchers(HttpMethod.GET, "/api/pedidos/alertas").hasRole("MESERO")
                    .requestMatchers(HttpMethod.GET, "/api/pedidos", "/api/pedidos/*")
                    .hasAnyRole("COCINA", "ADMINISTRADOR")
                    .requestMatchers(HttpMethod.PUT, "/api/pedidos/*").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.DELETE, "/api/pedidos/*").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado").hasRole("COCINA")
                    .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/entrega").hasRole("MESERO")
                    .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/recibido").hasRole("MESERO")
                    .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/cancelacion").hasRole("MESERO")
                    .requestMatchers("/api/administracion/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.GET, "/api/estados-pedido", "/api/estados-pedido/**")
                    .hasAnyRole("COCINA", "ADMINISTRADOR")
                    .requestMatchers("/api/estados-pedido/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.GET, "/api/detalles-pedido", "/api/detalles-pedido/**")
                    .hasAnyRole("MESERO", "COCINA", "ADMINISTRADOR")
                    .requestMatchers(HttpMethod.POST, "/api/detalles-pedido/**").hasRole("MESERO")
                    .requestMatchers("/api/detalles-pedido/**").hasAnyRole("MESERO", "ADMINISTRADOR")
                    .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**")
                    .hasAnyRole("MESERO", "COCINA", "ADMINISTRADOR")
                    .requestMatchers("/api/productos/**").hasRole("ADMINISTRADOR")
                    .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**")
                    .hasAnyRole("MESERO", "COCINA", "ADMINISTRADOR")
                    .requestMatchers("/api/categorias/**").hasRole("ADMINISTRADOR")
                    .anyRequest().permitAll())
            .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}