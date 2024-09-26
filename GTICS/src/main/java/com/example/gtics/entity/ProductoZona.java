package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="producto_zona")
public class ProductoZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idProductoZona;

    @ManyToOne
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "idZona", nullable = false)
    private Zona zona;

    @Column(name="cantidad", nullable = false)
    private Integer cantidad;

}