package com.poli.polirestaurante.dao;

import java.util.List;

public interface DAO<T> {

    T crear(T objeto);

    List<T> listar();

    T buscarPorId(Long id);

    T actualizar(T objeto);

    boolean eliminar(Long id);
}