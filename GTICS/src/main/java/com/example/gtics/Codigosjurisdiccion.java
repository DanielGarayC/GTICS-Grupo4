package com.example.gtics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "codigosjurisdiccion")
public class Codigosjurisdiccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCodigosJurisdiccion", nullable = false)
    private Integer id;

    @Column(name = "codigoJurisdiccion", nullable = false, length = 45)
    private String codigoJurisdiccion;

}