package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "controlorden")
public class ControlOrden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idControlOrden", nullable = false)
    private Integer id;

    @Column(name = "nombreControl", nullable = false, length = 45)
    private String nombreControl;
}
