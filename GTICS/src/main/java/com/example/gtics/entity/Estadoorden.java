package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "estadoorden")
public class Estadoorden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEstadoOrden", nullable = false)
    private Integer id;

    @Column(name = "nombreEstado", nullable = false, length = 45)
    private String nombreEstado;


}