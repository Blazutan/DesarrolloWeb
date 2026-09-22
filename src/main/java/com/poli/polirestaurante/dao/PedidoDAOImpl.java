package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.Pedido;
import com.poli.polirestaurante.repository.PedidoRepository;

@Repository
public class PedidoDAOImpl implements PedidoDAO {

    private final PedidoRepository pedidoRepository;

    @Autowired
    public PedidoDAOImpl(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public Pedido crear(Pedido objeto) {
        return pedidoRepository.save(objeto);
    }

    @Override
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    @Override
    public Pedido actualizar(Pedido objeto) {
        return pedidoRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            return false;
        }
        pedidoRepository.deleteById(id);
        return true;
    }
}