package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tarjetas")
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTarjeta", nullable = false)
    private Integer id;

    @Column(name = "nombreTitular", nullable = false, length = 100)
    private String nombreTitular;

    @Column(name = "numeroTarjetaHash", nullable = false, length = 255)
    private String numeroTarjetaHash;

    @Column(name = "ultimosDigitos", nullable = false, length = 4)
    private String ultimosDigitos;

    @Column(name = "fechaExpiracion", nullable = false, length = 5)
    private String fechaExpiracion;

    @Column(name = "fechaCreacion", nullable = false, updatable = false, insertable = false)
    private String fechaCreacion;
}
