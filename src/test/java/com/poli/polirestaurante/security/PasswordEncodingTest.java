package com.poli.polirestaurante.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncodingTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void encodeIfNeeded_codificaTextoPlano() {
        String encoded = PasswordEncoding.encodeIfNeeded("secreto", encoder);

        assertThat(encoded).isNotEqualTo("secreto");
        assertThat(encoder.matches("secreto", encoded)).isTrue();
    }

    @Test
    void encodeIfNeeded_conservaHashBcryptValido() {
        String existingHash = encoder.encode("secreto");

        assertThat(PasswordEncoding.encodeIfNeeded(existingHash, encoder)).isEqualTo(existingHash);
    }
}