package com.poli.polirestaurante.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.poli.polirestaurante.modelo.DetallePedido;
import com.poli.polirestaurante.repository.DetallePedidoRepository;

@Repository
public class DetallePedidoDAOImpl implements DetallePedidoDAO {

    private final DetallePedidoRepository detallePedidoRepository;

    @Autowired
    public DetallePedidoDAOImpl(DetallePedidoRepository detallePedidoRepository) {
        this.detallePedidoRepository = detallePedidoRepository;
    }

    @Override
    public DetallePedido crear(DetallePedido objeto) {
        return detallePedidoRepository.save(objeto);
    }

    @Override
    public List<DetallePedido> listar() {
        return detallePedidoRepository.findAll();
    }

    @Override
    public DetallePedido buscarPorId(Long id) {
        return detallePedidoRepository.findById(id).orElse(null);
    }

    @Override
    public DetallePedido actualizar(DetallePedido objeto) {
        return detallePedidoRepository.save(objeto);
    }

    @Override
    public boolean eliminar(Long id) {
        if (!detallePedidoRepository.existsById(id)) {
            return false;
        }
        detallePedidoRepository.deleteById(id);
        return true;
    }
}