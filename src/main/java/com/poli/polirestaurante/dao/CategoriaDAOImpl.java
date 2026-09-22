package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.repository.CategoriaRepository;

@Repository
public class CategoriaDAOImpl implements CategoriaDAO {

    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaDAOImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public Categoria crear(Categoria objeto) {
        return categoriaRepository.save(objeto);
    }

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Override
    public Categoria actualizar(Categoria objeto) {
        return categoriaRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            return false;
        }
        categoriaRepository.deleteById(id);
        return true;
    }
}