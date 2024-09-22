package com.example.gtics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "codigosaduaneros")
public class Codigosaduanero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCodigosAduaneros", nullable = false)
    private Integer id;

    @Column(name = "codigoAduanero", nullable = false, length = 45)
    private String codigoAduanero;

}