package com.example.gtics;

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

}