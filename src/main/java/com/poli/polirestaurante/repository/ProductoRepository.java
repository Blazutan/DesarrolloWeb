package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}