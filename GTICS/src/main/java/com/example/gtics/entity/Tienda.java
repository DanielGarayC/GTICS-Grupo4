package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tienda")
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTienda", nullable = false)
    private Integer idTienda;

    @Column(name = "nombreTienda", nullable = false, length = 45)
    private String nombreTienda;

    @Column(name = "idVendedor", nullable = false, length = 10)
    private String idVendedor;

    @Column(name = "emailVendedor", nullable = false, length = 100)
    private String emailVendedor;

    @Column(name = "ventasBrutas", nullable = false)
    private Double ventasBrutas;

    @Column(name = "ganancias", nullable = false)
    private Double ganancias;

    // Relación OneToMany con Proveedor
    @OneToMany(mappedBy = "tienda")
    private Set<Proveedor> proveedores = new LinkedHashSet<>();
}
