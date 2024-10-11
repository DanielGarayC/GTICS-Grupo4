package com.example.gtics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "resena")
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idResena", nullable = false)
    private Integer id;

    @NotBlank(message = "La opinión no puede estar vacía")
    @Size(max = 255, message = "La opinión no puede exceder los 255 caracteres")
    @Column(name = "opinion", length = 255)
    private String opinion;

    @NotBlank(message = "El tema no puede estar vacío")
    @Size(max = 100, message = "El tema no puede exceder los 100 caracteres")
    @Column(name = "tema", length = 100)
    private String tema;

    @NotNull(message = "Debe seleccionar si la entrega fue rápida")
    @Column(name = "entregaRapida", nullable = false)
    private Byte entregaRapida;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCalidad", nullable = false)
    private Calidad idCalidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idAtencion", nullable = false)
    private Atencion idAtencion;

    @NotNull(message = "Debe seleccionar un producto")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto producto;


    @Column(name = "fechaCreacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "util", nullable = false)
    private Integer util;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario idUsuario;

    @OneToMany(mappedBy = "idResena", cascade = CascadeType.ALL)
    @Size(min = 1, message = "Debe subir al menos una foto.")
    private List<Fotosresena> fotosresenas;

}
