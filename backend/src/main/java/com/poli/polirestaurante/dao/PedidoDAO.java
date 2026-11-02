// PedidoDAO.java
package com.poli.polirestaurante.dao;

import java.util.List;
import java.time.LocalDateTime;

import com.poli.polirestaurante.modelo.Pedido;

public interface PedidoDAO extends DAO<Pedido> {

	List<Pedido> listarPendientes();

	List<Pedido> listarAlertas(String correoMesero);

	List<Pedido> listarRecordatoriosVencidos(LocalDateTime ahora, int maximoRecordatorios);
}