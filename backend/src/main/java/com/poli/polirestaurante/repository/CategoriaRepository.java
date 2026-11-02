package com.poli.polirestaurante.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}