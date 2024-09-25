package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Estadoorden EstadoOrden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idControlOrden", nullable = false)
    private ControlOrden controlOrden;

    @Column(name = "fechaOrden", nullable = false)
    private LocalDate fechaOrden;

}