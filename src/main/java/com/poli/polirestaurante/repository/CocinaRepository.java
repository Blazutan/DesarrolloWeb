package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Cocina;

public interface CocinaRepository extends JpaRepository<Cocina, Long> {
}