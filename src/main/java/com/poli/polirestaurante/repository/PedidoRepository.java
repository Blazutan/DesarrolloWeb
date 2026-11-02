package com.poli.polirestaurante.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poli.polirestaurante.modelo.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstado_NombreIgnoreCaseOrderByFechaCreacionAsc(String nombreEstado);

    List<Pedido> findByMesero_CorreoIgnoreCaseAndEstado_NombreIgnoreCaseAndAlertaRecibidaFalseOrderByFechaListoAsc(
	    String correoMesero, String nombreEstado);

    List<Pedido> findByEstado_NombreIgnoreCaseAndAlertaRecibidaFalseAndNumeroRecordatoriosLessThanAndFechaProximoRecordatorioLessThanEqual(
	    String nombreEstado, int maximoRecordatorios, LocalDateTime ahora);
}