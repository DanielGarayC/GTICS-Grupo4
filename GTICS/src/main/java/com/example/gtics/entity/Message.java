package com.example.gtics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idmessage", nullable = false)
    private Integer id;

    @NotNull
    @Lob
    @Column(name = "contenido", nullable = false)
    private String contenido;

    @Size(max = 45)
    @NotNull
    @Column(name = "sala", nullable = false, length = 45)
    private String sala;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario idUsuario;

}