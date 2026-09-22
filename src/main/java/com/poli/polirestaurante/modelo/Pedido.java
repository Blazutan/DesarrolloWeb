package com.poli.polirestaurante.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    @JoinColumn (name = "cocina_id", nullable = false)
    private Cocina cocina;

    @ManyToOne 
    @JoinColumn (name = "estado_id", nullable = false)
    private EstadoPedido estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnPreparacion;
    private LocalDateTime fechaListo;
    private LocalDateTime fechaEntregado;

    private String motivoCancelacion;

    public Pedido() {
    }
    public Pedido(Mesero mesero, EstadoPedido estado) {
        this.mesero = mesero;
        this.estado = estado;
        this.fechaCreacion = LocalDateTime.now();
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

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

}
