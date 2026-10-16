// CocinaDAOImpl.java
package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Cocina;
import com.poli.polirestaurante.repository.CocinaRepository;
import com.poli.polirestaurante.security.PasswordEncoding;

@Repository
public class CocinaDAOImpl implements CocinaDAO {

    private final CocinaRepository cocinaRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CocinaDAOImpl(CocinaRepository cocinaRepository, PasswordEncoder passwordEncoder) {
        this.cocinaRepository = cocinaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Cocina crear(Cocina objeto) {
        codificarPassword(objeto);
        return cocinaRepository.save(objeto);
    }

    @Override
    public List<Cocina> listar() {
        return cocinaRepository.findAll();
    }

    @Override
    public Cocina buscarPorId(Long id) {
        return cocinaRepository.findById(id).orElse(null);
    }

    @Override
    public Cocina actualizar(Cocina objeto) {
        codificarPassword(objeto);
        return cocinaRepository.save(objeto);
    }

    private void codificarPassword(Cocina cocina) {
        cocina.setPassword(PasswordEncoding.encodeIfNeeded(cocina.getPassword(), passwordEncoder));
    }

    @Override
    public boolean eliminar(Long id) {
        if (!cocinaRepository.existsById(id)) {
            return false;
        }
        cocinaRepository.deleteById(id);
        return true;
    }
}