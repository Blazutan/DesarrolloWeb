// CocinaDAOImpl.java
package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Cocina;
import com.poli.polirestaurante.repository.CocinaRepository;

@Repository
public class CocinaDAOImpl implements CocinaDAO {

    private final CocinaRepository cocinaRepository;

    @Autowired
    public CocinaDAOImpl(CocinaRepository cocinaRepository) {
        this.cocinaRepository = cocinaRepository;
    }

    @Override
    public Cocina crear(Cocina objeto) {
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
        return cocinaRepository.save(objeto);
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