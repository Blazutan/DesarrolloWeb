// MeseroRepository.java
package com.poli.polirestaurante.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Mesero;

public interface MeseroRepository extends JpaRepository<Mesero, Long> {

	Optional<Mesero> findByCorreo(String correo);
}