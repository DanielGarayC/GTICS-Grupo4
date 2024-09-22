package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idUsuario", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 45)
    private String apellido;

    @Column(name = "email", nullable = false, length = 45)
    private String email;

    @Column(name = "direccion", nullable = false, length = 45)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idDistrito", nullable = false)
    private Distrito idDistrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idRol", nullable = false)
    private Rol idRol;

    @Column(name = "baneado")
    private Byte baneado;

    @Column(name = "AGT_codigoAduana", length = 45)
    private String agtCodigoaduana;

    @Column(name = "AGT_ruc", length = 45)
    private String agtRuc;

    @Column(name = "AGT_razonSocial", length = 45)
    private String agtRazonsocial;

    @Column(name = "AZ_fechaNacimiento")
    private LocalDate azFechanacimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idSolicitudAgente", nullable = false)
    private Solicitudagente idSolicitudAgente;

    @Column(name = "AGT_codigoJurisdiccion", length = 45)
    private String agtCodigojurisdiccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAgente")
    private Usuario idAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAdminZonal")
    private Usuario idAdminZonal;

    @Column(name = "foto")
    private byte[] foto;

    @Column(name = "`contraseña`", nullable = false, length = 45)
    private String contraseña;

    @Column(name="telefono", nullable = false, length = 45)
    private String telefono;

    @Column(name="dni", nullable = false, length = 45)
    private String dni;


}