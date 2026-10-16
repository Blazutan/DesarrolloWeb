package com.poli.polirestaurante.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Administrador;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

	Optional<Administrador> findByCorreo(String correo);
}