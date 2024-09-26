package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "resena")
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idResena", nullable = false)
    private Integer id;

    @Column(name = "opinion", length = 45)
    private String opinion;

    @Column(name = "entregaRapida", nullable = false)
    private Byte entregaRapida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCalidad", nullable = false)
    private Calidad idCalidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idAtencion", nullable = false)
    private Atencion idAtencion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto Producto;

}