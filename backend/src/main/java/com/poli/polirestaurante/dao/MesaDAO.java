package com.poli.polirestaurante.dao;

import java.util.Optional;

import com.poli.polirestaurante.modelo.Mesa;

public interface MesaDAO extends DAO<Mesa> {

    Optional<Mesa> buscarPorPedidoId(Long pedidoId);
}