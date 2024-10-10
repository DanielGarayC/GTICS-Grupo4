package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "orden")
public class Orden {
    @Id
    @Column(name = "idOrden", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgente")
    private Usuario idAgente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCarritoCompra", nullable = false)
    private Carritocompra idCarritoCompra;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idEstadoOrden", nullable = false)
    private Estadoorden estadoorden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idControlOrden", nullable = false)
    private ControlOrden controlOrden;

    @Column(name = "fechaOrden", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaOrden;

    @Column(name = "solicitarCancelarOrden")
    private Integer solicitarCancelarOrden;

    @Column(name = "ordenEliminada")
    private Integer ordenEliminada;

    @Column(name = "razonEliminacion")
    private String razonEliminacion;

    @Column(name = "costosAdicionales")
    private Double costosAdicionales;

}