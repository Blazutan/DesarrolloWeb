package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}