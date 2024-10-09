package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCategoria", nullable = false)
    private Integer id;

    @Column(name = "nombreCategoria", nullable = false, length = 45)
    private String nombreCategoria;

    @Lob
    @Column(name = "fotoCategoria", nullable = false)
    private byte[] fotoCategoria;

    @OneToMany(mappedBy = "categoria", fetch = FetchType.EAGER)
    private List<Subcategoria> subcategorias;

}