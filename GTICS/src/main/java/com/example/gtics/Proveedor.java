package com.example.gtics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

}