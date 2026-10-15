package com.poli.polirestaurante.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.poli.polirestaurante.dao.CategoriaDAO;
import com.poli.polirestaurante.modelo.Categoria;

@Service
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaService(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public Categoria crear(Categoria categoria) {
        categoria.setId(null);
        return categoriaDAO.crear(categoria);
    }

    public List<Categoria> listar() {
        return categoriaDAO.listar();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaDAO.buscarPorId(id);
    }

    public Categoria actualizar(Long id, Categoria datos) {
        Categoria existente = categoriaDAO.buscarPorId(id);
        if (existente == null) {
            return null;
        }
        existente.setNombre(datos.getNombre());
        return categoriaDAO.actualizar(existente);
    }

    public boolean eliminar(Long id) {
        return categoriaDAO.eliminar(id);
    }
}