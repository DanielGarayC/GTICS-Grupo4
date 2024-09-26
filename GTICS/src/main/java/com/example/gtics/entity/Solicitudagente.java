package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "solicitudagente")
public class Solicitudagente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSolicitudAgente", nullable = false)
    private Integer id;

    @Column(name = "codigoAduana", nullable = false, length = 45)
    private String codigoAduana;

    @Column(name = "codigoJurisdiccion", nullable = false, length = 45)
    private String codigoJurisdiccion;

    @Column(name = "indicadorSolicitud", nullable = true)
    private Integer indicadorSolicitud;


    //@ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "ValidacionesCodigosAgente_idValidacionesCodigosAgente", nullable = false)
    @Column(name = "ValidacionesCodigosAgente_idValidacionesCodigosAgente", nullable = true)
    private Integer validaciones;

}