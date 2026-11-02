package com.poli.polirestaurante.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.poli.polirestaurante.modelo.Cocina;
import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.repository.AdministradorRepository;
import com.poli.polirestaurante.repository.CocinaRepository;
import com.poli.polirestaurante.repository.MeseroRepository;

@ExtendWith(MockitoExtension.class)
class RestauranteUserDetailsServiceTest {

    @Mock
    private MeseroRepository meseroRepository;

    @Mock
    private CocinaRepository cocinaRepository;

        @Mock
        private AdministradorRepository administradorRepository;

    @InjectMocks
    private RestauranteUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_asignaRolMeseroAlCorreoRegistrado() {
        when(meseroRepository.findByCorreo("mesero@restaurante.com"))
                .thenReturn(Optional.of(new Mesero("Ana", "mesero@restaurante.com", "{bcrypt}hash")));

        var usuario = userDetailsService.loadUserByUsername("mesero@restaurante.com");

        assertThat(usuario.getAuthorities()).extracting("authority").containsExactly("ROLE_MESERO");
        assertThat(usuario.getPassword()).isEqualTo("{bcrypt}hash");
    }

    @Test
    void loadUserByUsername_asignaRolCocinaAlCorreoRegistrado() {
        when(cocinaRepository.findByCorreo("cocina@restaurante.com"))
                .thenReturn(Optional.of(new Cocina("Luis", "cocina@restaurante.com", "{bcrypt}hash")));

        var usuario = userDetailsService.loadUserByUsername("cocina@restaurante.com");

        assertThat(usuario.getAuthorities()).extracting("authority").containsExactly("ROLE_COCINA");
    }

    @Test
    void loadUserByUsername_rechazaCorreosDuplicadosEntreRoles() {
        when(meseroRepository.findByCorreo("duplicado@restaurante.com"))
                .thenReturn(Optional.of(new Mesero("Ana", "duplicado@restaurante.com", "{bcrypt}hash")));
        when(cocinaRepository.findByCorreo("duplicado@restaurante.com"))
                .thenReturn(Optional.of(new Cocina("Luis", "duplicado@restaurante.com", "{bcrypt}hash")));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("duplicado@restaurante.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}