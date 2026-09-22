// MeseroRepository.java
package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Mesero;

public interface MeseroRepository extends JpaRepository<Mesero, Long> {
}