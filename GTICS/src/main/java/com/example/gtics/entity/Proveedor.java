package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "proveedor")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProveedor", nullable = false)
    private Integer id;

    @Column(name = "nombreProveedor", nullable = false, length = 45)
    private String nombreProveedor;

    @Column(name = "contacto", nullable = false, length = 45)
    private String contacto;

    @Column(name = "ruc", nullable = false, length = 45)
    private String ruc;

    @Column(name = "dni", nullable = false, length = 45)
    private String dni;

    @Column(name = "nombreTienda", nullable = false, length = 45)
    private String nombreTienda;

    @OneToMany(mappedBy = "idProveedor")
    private Set<Producto> productos = new LinkedHashSet<>();

}