package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.EstadoPedido;

public interface EstadoPedidoRepository extends JpaRepository<EstadoPedido, Long> {
}