package com.poli.polirestaurante.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.poli.polirestaurante.dao.CategoriaDAO;
import com.poli.polirestaurante.dao.ProductoDAO;
import com.poli.polirestaurante.dto.ProductoRequest;
import com.poli.polirestaurante.modelo.Categoria;
import com.poli.polirestaurante.modelo.Producto;

@Service
public class ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    public ProductoService(ProductoDAO productoDAO, CategoriaDAO categoriaDAO) {
        this.productoDAO = productoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public List<Producto> listar() {
        return productoDAO.listar();
    }

    public Producto buscarPorId(Long id) {
        return productoDAO.buscarPorId(id);
    }

    public Producto crear(ProductoRequest datos) {
        validar(datos);
        Producto producto = new Producto(datos.nombre().trim(), datos.descripcion(), datos.precio(),
                datos.disponibilidad(), buscarCategoria(datos.categoriaId()));
        return productoDAO.crear(producto);
    }

    public Producto actualizar(Long id, ProductoRequest datos) {
        validar(datos);
        Producto producto = productoDAO.buscarPorId(id);
        if (producto == null) {
            return null;
        }
        producto.setNombre(datos.nombre().trim());
        producto.setDescripcion(datos.descripcion());
        producto.actualizarPrecio(datos.precio());
        producto.setDisponibilidad(datos.disponibilidad());
        producto.setCategoria(buscarCategoria(datos.categoriaId()));
        return productoDAO.actualizar(producto);
    }

    public boolean eliminar(Long id) {
        return productoDAO.eliminar(id);
    }

    private Categoria buscarCategoria(Long id) {
        Categoria categoria = categoriaDAO.buscarPorId(id);
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría indicada no existe.");
        }
        return categoria;
    }

    private void validar(ProductoRequest datos) {
        if (datos == null || datos.nombre() == null || datos.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (datos.precio() == null || datos.precio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual a cero.");
        }
        if (datos.categoriaId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
    }
}