package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Producto;
import com.poli.polirestaurante.repository.ProductoRepository;

@Repository
public class ProductoDAOImpl implements ProductoDAO {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoDAOImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public Producto crear(Producto objeto) {
        return productoRepository.save(objeto);
    }

    @Override
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Override
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    public Producto actualizar(Producto objeto) {
        return productoRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }
}