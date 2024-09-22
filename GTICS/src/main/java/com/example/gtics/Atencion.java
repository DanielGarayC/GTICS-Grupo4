package com.example.gtics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "atencion")
public class Atencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAtencion", nullable = false)
    private Integer id;

    @Column(name = "opcionesAtencion", nullable = false, length = 45)
    private String opcionesAtencion;

}