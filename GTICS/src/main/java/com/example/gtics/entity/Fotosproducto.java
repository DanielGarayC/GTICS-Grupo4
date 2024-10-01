package com.example.gtics.entity;

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

    @Lob
    @Column(name = "foto", nullable = false)
    private byte[] foto;

    @Column(name = "fotoNombre", length = 255)
    private String fotoNombre;

    @Column(name = "fotoContentType", length = 255)
    private String fotoContentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto producto;

}
