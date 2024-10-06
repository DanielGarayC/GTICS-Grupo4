package com.example.gtics.entity;

import com.example.gtics.entity.Foropregunta;
import com.example.gtics.entity.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "fororespuesta")
public class Fororespuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRespuesta", nullable = false)
    private Integer id;

    @NotNull
    @Lob
    @Column(name = "contenidoRespuesta", nullable = false)
    private String contenidoRespuesta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idPregunta", nullable = false)
    private Foropregunta idPregunta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario idUsuario;

    @NotNull
    @Column(name = "fechaRespuesta", nullable = false)
    private LocalDate fechaRespuesta;

}