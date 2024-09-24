package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProducto", nullable = false)
    private Integer id;

    @Column(name = "nombreProducto", nullable = false, length = 45)
    private String nombreProducto;

    @Column(name = "cantidadDisponible", nullable = false, length = 45)
    private String cantidadDisponible;

    @Lob
    @Column(name = "`descripción`", nullable = false)
    private String descripción;

    @Lob
    @Column(name = "detallesTecnicos")
    private String detallesTecnicos;

    @Column(name = "precio", nullable = false, length = 45)
    private String precio;

    @Column(name = "costoEnvio", nullable = false, length = 45)
    private String costoEnvio;

    @Column(name = "modelo", length = 45)
    private String modelo;

    @Column(name = "color", length = 45)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria idCategoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProveedor", nullable = false)
    private Proveedor idProveedor;

}