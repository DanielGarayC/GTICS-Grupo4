package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "distrito")
public class Distrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddistrito", nullable = false)
    private Integer id;

    @Column(name = "nombredistrito", nullable = false, length = 45)
    private String nombre;

    @Column(name = "codigojurisdiccion", nullable = false, length = 45)
    private Integer codigojurisdiccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idzona", nullable = false)
    private Zona zona;


}