package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orden")
public class Orden {
    @Id
    @Column(name = "idOrden", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgente")
    private Usuario idAgente;

}