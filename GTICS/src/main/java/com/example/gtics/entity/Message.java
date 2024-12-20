package com.example.gtics.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

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
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "idUsuario", nullable = false)
    @JsonIgnore
    private Usuario idUsuario;

    @NotNull
    @Column(name = "fechaEnvio", nullable = false)
    private LocalDateTime fechaEnvio;

}