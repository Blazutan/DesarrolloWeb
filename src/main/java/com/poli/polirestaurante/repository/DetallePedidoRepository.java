package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.DetallePedido;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}