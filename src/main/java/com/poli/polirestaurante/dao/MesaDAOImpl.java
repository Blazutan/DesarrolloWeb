package com.poli.polirestaurante.dao;

import com.poli.polirestaurante.modelo.Mesa;
import com.poli.polirestaurante.repository.MesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MesaDAOImpl implements MesaDAO {

    private final MesaRepository mesaRepository;

    @Autowired
    public MesaDAOImpl(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    @Override
    public Mesa crear(Mesa objeto) {
        return mesaRepository.save(objeto);
    }

    @Override
    public List<Mesa> listar() {
        return mesaRepository.findAll();
    }

    @Override
    public Mesa buscarPorId(Long id) {
        return mesaRepository.findById(id).orElse(null);
    }

    @Override
    public Mesa actualizar(Mesa objeto) {
        return mesaRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!mesaRepository.existsById(id)) {
            return false;
        }
        mesaRepository.deleteById(id);
        return true;
    }
}