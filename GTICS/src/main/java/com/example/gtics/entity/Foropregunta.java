package com.example.gtics.entity;

import com.example.gtics.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "foropregunta")
public class Foropregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPregunta", nullable = false)
    private Integer id;

    @NotNull
    @Lob
    @Column(name = "pregunta", nullable = false)
    private String pregunta;

    @NotNull
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDate fechaCreacion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario idUsuario;

}