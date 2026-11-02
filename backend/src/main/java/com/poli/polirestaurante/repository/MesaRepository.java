package com.poli.polirestaurante.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    Optional<Mesa> findFirstByPedido_Id(Long pedidoId);
}