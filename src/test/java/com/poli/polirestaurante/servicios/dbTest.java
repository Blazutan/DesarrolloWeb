package com.poli.polirestaurante.servicios;

import java.sql.Connection;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void laConexionABaseDeDatosDeberiaEstarActiva() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(2)).isTrue();
        }
    }

    @Test
    void laBaseDeDatosDeberiaSerPostgreSQL() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String nombreProducto = connection.getMetaData().getDatabaseProductName();
            assertThat(nombreProducto).isEqualToIgnoringCase("PostgreSQL");
        }
    }
}