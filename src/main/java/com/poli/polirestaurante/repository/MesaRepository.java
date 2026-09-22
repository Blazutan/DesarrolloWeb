package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
}