package com.example.gtics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "solicitudagente")
public class Solicitudagente {
    @Id
    @Column(name = "idSolicitudAgente", nullable = false)
    private Integer id;

    @Column(name = "codigoAduana", nullable = false, length = 45)
    private String codigoAduana;

    @Column(name = "codigoJurisdiccion", nullable = false, length = 45)
    private String codigoJurisdiccion;

    @Column(name = "indicadorSolicitud", nullable = false)
    private Integer indicadorSolicitud;

}