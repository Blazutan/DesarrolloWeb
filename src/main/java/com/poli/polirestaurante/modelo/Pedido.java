package com.poli.polirestaurante.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;

@Entity 
@Table (name="pedido")
public class Pedido {
    
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY) 
    private Long id;

    @ManyToOne
    @JoinColumn (name = "mesero_id", nullable = false) 
    private Mesero mesero;

    @ManyToOne
    @JoinColumn(name = "cocina_id")
    private Cocina cocina;

    @ManyToOne 
    @JoinColumn (name = "estado_id", nullable = false)
    private EstadoPedido estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltimaActualizacion;
    private LocalDateTime fechaEnPreparacion;
    private LocalDateTime fechaListo;
    private LocalDateTime fechaEntregado;
    private LocalDateTime fechaProximoRecordatorio;
    private int numeroRecordatorios;
    private boolean alertaRecibida;
    private String motivoCancelacion;

    @jakarta.persistence.Column(precision = 12, scale = 2)
    private BigDecimal precioTotal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido")
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }
    public Pedido(Mesero mesero, EstadoPedido estado) {
        this.mesero = mesero;
        this.estado = estado;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaUltimaActualizacion = this.fechaCreacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Mesero getMesero() {
        return mesero;
    }

    public void setMesero(Mesero mesero) {
        this.mesero = mesero;
    }

    public Cocina getCocina() {
        return cocina;
    }

    public void setCocina(Cocina cocina) {
        this.cocina = cocina;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltimaActualizacion() {
        return fechaUltimaActualizacion;
    }

    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) {
        this.fechaUltimaActualizacion = fechaUltimaActualizacion;
    }

    public LocalDateTime getFechaEnPreparacion() {
        return fechaEnPreparacion;
    }

    public void setFechaEnPreparacion(LocalDateTime fechaEnPreparacion) {
        this.fechaEnPreparacion = fechaEnPreparacion;
    }

    public LocalDateTime getFechaListo() {
        return fechaListo;
    }

    public void setFechaListo(LocalDateTime fechaListo) {
        this.fechaListo = fechaListo;
    }

    public LocalDateTime getFechaEntregado() {
        return fechaEntregado;
    }

    public void setFechaEntregado(LocalDateTime fechaEntregado) {
        this.fechaEntregado = fechaEntregado;
    }

    public LocalDateTime getFechaProximoRecordatorio() {
        return fechaProximoRecordatorio;
    }

    public void setFechaProximoRecordatorio(LocalDateTime fechaProximoRecordatorio) {
        this.fechaProximoRecordatorio = fechaProximoRecordatorio;
    }

    public int getNumeroRecordatorios() {
        return numeroRecordatorios;
    }

    public void setNumeroRecordatorios(int numeroRecordatorios) {
        this.numeroRecordatorios = numeroRecordatorios;
    }

    public boolean isAlertaRecibida() {
        return alertaRecibida;
    }

    public void setAlertaRecibida(boolean alertaRecibida) {
        this.alertaRecibida = alertaRecibida;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal == null ? BigDecimal.ZERO : precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void agregarDetalle(DetallePedido detalle) {
        Objects.requireNonNull(detalle, "El detalle del pedido es obligatorio.");
        detalles.add(detalle);
        detalle.setPedido(this);
        precioTotal = calcularTotal();
    }

    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetallePedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
