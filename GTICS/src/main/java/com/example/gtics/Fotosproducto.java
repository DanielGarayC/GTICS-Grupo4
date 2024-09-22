package com.example.gtics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fotosproducto")
public class Fotosproducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFotosProducto", nullable = false)
    private Integer id;

    @Column(name = "foto", nullable = false)
    private byte[] foto;

}