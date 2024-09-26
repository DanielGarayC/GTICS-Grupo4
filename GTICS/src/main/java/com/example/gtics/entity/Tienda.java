package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tienda")
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTienda", nullable = false)
    private Integer id;

    @Column(name = "nombreTienda", nullable = false, length = 45)
    private String nombreTienda;

    @Column(name = "idVendedor", nullable = false, length = 45)
    private String idVendedor;

    @Column(name = "emailVendedor", nullable = false, length = 45)
    private String emailVendedor;

    @Column(name = "ventasBrutas", nullable = false, length = 45)
    private String ventasBrutas;

    @Column(name = "ganancias", nullable = false, length = 45)
    private String ganancias;

}
