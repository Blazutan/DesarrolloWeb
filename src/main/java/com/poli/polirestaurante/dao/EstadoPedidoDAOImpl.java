package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.EstadoPedido;
import com.poli.polirestaurante.repository.EstadoPedidoRepository;

@Repository
public class EstadoPedidoDAOImpl implements EstadoPedidoDAO {

    private final EstadoPedidoRepository estadoPedidoRepository;

    @Autowired
    public EstadoPedidoDAOImpl(EstadoPedidoRepository estadoPedidoRepository) {
        this.estadoPedidoRepository = estadoPedidoRepository;
    }

    @Override
    public EstadoPedido crear(EstadoPedido objeto) {
        return estadoPedidoRepository.save(objeto);
    }

    @Override
    public List<EstadoPedido> listar() {
        return estadoPedidoRepository.findAll();
    }

    @Override
    public EstadoPedido buscarPorId(Long id) {
        return estadoPedidoRepository.findById(id).orElse(null);
    }

    @Override
    public EstadoPedido actualizar(EstadoPedido objeto) {
        return estadoPedidoRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!estadoPedidoRepository.existsById(id)) {
            return false;
        }
        estadoPedidoRepository.deleteById(id);
        return true;
    }
}