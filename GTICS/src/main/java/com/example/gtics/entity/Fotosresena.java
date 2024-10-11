package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fotosresena")
public class Fotosresena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFotosResena", nullable = false)
    private Integer id;

    @Column(name = "foto", nullable = false)
    private byte[] foto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idResena", nullable = false)
    private Resena idResena;

    @Column(name = "tipo", nullable = false)
    private String tipo;  // Aquí almacenamos el tipo MIME de la imagen (e.g., image/jpeg)


}
