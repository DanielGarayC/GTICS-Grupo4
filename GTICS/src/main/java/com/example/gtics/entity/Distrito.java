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
@Table(name = "distrito")
public class Distrito {
    @Id
    @Column(name = "idDistrito", nullable = false)
    private Integer id;

    @Column(name = "nombreDistrito", nullable = false, length = 45)
    private String nombreDistrito;

}