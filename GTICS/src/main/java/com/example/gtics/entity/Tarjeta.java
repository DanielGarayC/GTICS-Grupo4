package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tarjetas")
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTarjeta", nullable = false)
    private Integer id;

    @Column(name = "cvv", nullable = false, length = 3)
    private String cvv;

    @Column(name = "titular", nullable = false, length = 100)
    private String titular;

    @Column(name = "fechaExpiracion", nullable = false)
    private LocalDate fechaExpiracion;

    @Column(name = "numeroTarjeta", nullable = false, length = 16)
    private String numeroTarjeta;

}