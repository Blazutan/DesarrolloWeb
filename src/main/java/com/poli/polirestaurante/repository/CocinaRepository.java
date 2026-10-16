package com.poli.polirestaurante.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Cocina;

public interface CocinaRepository extends JpaRepository<Cocina, Long> {

	Optional<Cocina> findByCorreo(String correo);
}