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
    @Column(name = "idusuario", nullable = false)
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
    @JoinColumn(name = "iddistrito", nullable = false)
    private Distrito idDistrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idrol", nullable = false)
    private Rol rol;

    @Column(name = "baneado")
    private Byte baneado;

    @Column(name = "agt_codigoaduana", length = 45)
    private String agtCodigoaduana;

    @Column(name = "agt_ruc", length = 45)
    private String agtRuc;

    @Column(name = "agt_razonsocial", length = 45)
    private String agtRazonsocial;

    @Column(name = "az_fechanacimiento")
    private LocalDate azFechanacimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idsolicitudagente", nullable = false)
    private Solicitudagente idSolicitudAgente;

    @Column(name = "agt_codigojurisdiccion", length = 45)
    private String agtCodigojurisdiccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idagente")
    private Usuario idAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idadminzonal")
    private Usuario idAdminZonal;

    @Column(name = "foto")
    private byte[] foto;

    @Column(name = "`contraseña`", nullable = false, length = 45)
    private String contraseña;

    @Column(name = "telefono", nullable = false, length = 45)
    private String telefono;

    @Column(name = "dni", nullable = false, length = 45)
    private String dni;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idzona", nullable = false)
    private Zona zona;

}