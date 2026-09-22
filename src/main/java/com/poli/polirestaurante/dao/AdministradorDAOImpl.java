// AdministradorDAOImpl.java
package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Administrador;
import com.poli.polirestaurante.repository.AdministradorRepository;

@Repository
public class AdministradorDAOImpl implements AdministradorDAO {

    private final AdministradorRepository administradorRepository;

    @Autowired
    public AdministradorDAOImpl(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    @Override
    public Administrador crear(Administrador objeto) {
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
        return administradorRepository.save(objeto);
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