package com.example.gtics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "calidad")
public class Calidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCalidad", nullable = false)
    private Integer id;

    @Column(name = "opcionesCalidad", nullable = false, length = 45)
    private String opcionesCalidad;

}