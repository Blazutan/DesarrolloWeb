package com.poli.polirestaurante.dao;

import java.time.LocalDateTime;
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
    public List<Pedido> listarPendientes() {
        return pedidoRepository.findByEstado_NombreIgnoreCaseOrderByFechaCreacionAsc("PENDIENTE");
    }

    @Override
    public List<Pedido> listarAlertas(String correoMesero) {
        return pedidoRepository
                .findByMesero_CorreoIgnoreCaseAndEstado_NombreIgnoreCaseAndAlertaRecibidaFalseOrderByFechaListoAsc(
                        correoMesero, "LISTO");
    }

    @Override
    public List<Pedido> listarRecordatoriosVencidos(LocalDateTime ahora, int maximoRecordatorios) {
        return pedidoRepository
                .findByEstado_NombreIgnoreCaseAndAlertaRecibidaFalseAndNumeroRecordatoriosLessThanAndFechaProximoRecordatorioLessThanEqual(
                        "LISTO", maximoRecordatorios, ahora);
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