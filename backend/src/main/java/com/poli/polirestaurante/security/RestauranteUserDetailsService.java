package com.poli.polirestaurante.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.poli.polirestaurante.repository.AdministradorRepository;
import com.poli.polirestaurante.repository.CocinaRepository;
import com.poli.polirestaurante.repository.MeseroRepository;

@Service
public class RestauranteUserDetailsService implements UserDetailsService {

    private final MeseroRepository meseroRepository;
    private final CocinaRepository cocinaRepository;
    private final AdministradorRepository administradorRepository;

    public RestauranteUserDetailsService(MeseroRepository meseroRepository, CocinaRepository cocinaRepository,
            AdministradorRepository administradorRepository) {
        this.meseroRepository = meseroRepository;
        this.cocinaRepository = cocinaRepository;
        this.administradorRepository = administradorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        List<UserDetails> coincidencias = new ArrayList<>();
        meseroRepository.findByCorreo(correo)
                .ifPresent(mesero -> coincidencias.add(crearUsuario(mesero.getCorreo(), mesero.getPassword(), "MESERO")));
        cocinaRepository.findByCorreo(correo)
                .ifPresent(cocina -> coincidencias.add(crearUsuario(cocina.getCorreo(), cocina.getPassword(), "COCINA")));
        administradorRepository.findByCorreo(correo).ifPresent(administrador -> coincidencias.add(
            crearUsuario(administrador.getCorreo(), administrador.getPassword(), "ADMINISTRADOR")));

        if (coincidencias.size() != 1) {
            throw new UsernameNotFoundException("No existe un usuario único con ese correo.");
        }
        return coincidencias.get(0);
    }

    private UserDetails crearUsuario(String correo, String password, String rol) {
        return User.withUsername(correo).password(password).roles(rol).build();
    }
}