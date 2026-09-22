package com.poli.polirestaurante.dao;

import com.poli.polirestaurante.modelo.Mesero;
import com.poli.polirestaurante.repository.MeseroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MeseroDAOImpl implements MeseroDAO {

    private final MeseroRepository meseroRepository;

    @Autowired
    public MeseroDAOImpl(MeseroRepository meseroRepository) {
        this.meseroRepository = meseroRepository;
    }

    @Override
    public Mesero crear(Mesero objeto) {
        return meseroRepository.save(objeto);
    }

    @Override
    public List<Mesero> listar() {
        return meseroRepository.findAll();
    }

    @Override
    public Mesero buscarPorId(Long id) {
        return meseroRepository.findById(id).orElse(null);
    }

    @Override
    public Mesero actualizar(Mesero objeto) {
        return meseroRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!meseroRepository.existsById(id)) {
            return false;
        }
        meseroRepository.deleteById(id);
        return true;
    }
}