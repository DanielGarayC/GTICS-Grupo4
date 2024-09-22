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
@Table(name = "rol")
public class Rol {
    @Id
    @Column(name = "idRol", nullable = false)
    private Integer id;

    @Column(name = "nombreRol", nullable = false, length = 45)
    private String nombreRol;

}