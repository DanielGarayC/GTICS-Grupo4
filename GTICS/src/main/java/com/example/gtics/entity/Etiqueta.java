package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "etiquetas")
public class Etiqueta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idetiquetas")
    private Integer idEtiqueta;

    @Column(name = "nombreetiqueta", nullable = false, length = 50)
    private String nombreEtiqueta;  // Nombre de la etiqueta (ej. Casa, Oficina)

    // Nueva relación con el usuario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;  // Relación con la tabla `usuario`, cada etiqueta pertenece a un usuario específico
}
