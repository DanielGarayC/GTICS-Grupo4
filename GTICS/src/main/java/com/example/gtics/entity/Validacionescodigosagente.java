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
@Table(name = "validacionescodigosagente")
public class Validacionescodigosagente {
    @Id
    @Column(name = "idValidacionesCodigosAgente", nullable = false)
    private Integer id;

    @Column(name = "codigoAduanaValidado", nullable = false, length = 45)
    private String codigoAduanaValidado;

    @Column(name = "codigoJurisdiccionValidado", nullable = false, length = 45)
    private String codigoJurisdiccionValidado;

}