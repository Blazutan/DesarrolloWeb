// AdministradorDAOImpl.java
package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Administrador;
import com.poli.polirestaurante.repository.AdministradorRepository;
import com.poli.polirestaurante.security.PasswordEncoding;

@Repository
public class AdministradorDAOImpl implements AdministradorDAO {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdministradorDAOImpl(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Administrador crear(Administrador objeto) {
        codificarPassword(objeto);
        return administradorRepository.save(objeto);
    }

    @Override
    public List<Administrador> listar() {
        return administradorRepository.findAll();
    }

    @Override
    public Administrador buscarPorId(Long id) {
        return administradorRepository.findById(id).orElse(null);
    }

    @Override
    public Administrador actualizar(Administrador objeto) {
        codificarPassword(objeto);
        return administradorRepository.save(objeto);
    }

    private void codificarPassword(Administrador administrador) {
        administrador.setPassword(PasswordEncoding.encodeIfNeeded(administrador.getPassword(), passwordEncoder));
    }

    @Override
    public boolean eliminar(Long id) {
        if (!administradorRepository.existsById(id)) {
            return false;
        }
        administradorRepository.deleteById(id);
        return true;
    }
}