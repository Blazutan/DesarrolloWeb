// PedidoDAO.java
package com.poli.polirestaurante.dao;

import java.util.List;

import com.poli.polirestaurante.modelo.Pedido;

public interface PedidoDAO extends DAO<Pedido> {

	List<Pedido> listarPendientes();
}