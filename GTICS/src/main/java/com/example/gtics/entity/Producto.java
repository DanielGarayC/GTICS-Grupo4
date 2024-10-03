package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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
    private Integer cantidadDisponible;

    @Lob
    @Column(name = "`descripcion`", nullable = false)
    private String descripcion;

    @Column(name = "precio", nullable = false, length = 45)
    private Double precio;

    @Column(name = "costoEnvio", nullable = false, length = 45)
    private Double costoEnvio;

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


    @Column(name = "cantVentas", nullable = false, length = 45)
    private Integer cantVentas;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idSubcategoria", nullable = true)
    private Subcategoria idSubcategoria;

    @Column(name = "borrado", nullable = false)
    private Integer borrado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zona_idZona", nullable = false)
    private Zona zona;

    @Column(name = "fechaArribo")
    private LocalDate fechaArribo;

}