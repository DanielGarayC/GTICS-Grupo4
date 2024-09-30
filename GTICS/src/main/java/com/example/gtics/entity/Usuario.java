package com.example.gtics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @Column(name = "idusuario")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar un nombre")
    @Size(max = 40, message = "El nombre no puede tener más de 40 caracteres")
    private String nombre;


    @Column(name = "email", nullable = false, length = 45)
    @Email(message = "Debe ingresar un correo electrónico válido")
    @NotBlank(message = "Debe ingresar un correo electrónico")
    private String email;

    @Column(name = "direccion", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar una dirección")
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iddistrito")
    private Distrito distrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idrol", nullable = false)
    private Rol rol;

    @Column(name = "baneado")
    private boolean baneado;

    @Column(name = "agt_codigoaduana", length = 45)
    @Size(min = 3, max = 3, message = "El código aduanero debe tener exactamente 3 dígitos")
    @NotBlank(message = "El código aduanero no puede estar vacío")
    @Pattern(regexp = "\\d+", message = "El código aduanero debe contener solo dígitos")
    private String agtCodigoaduana;

    @Column(name = "agt_ruc", length = 45)
    @Size(min = 10, max = 10, message = "El RUC debe tener exactamente 10 dígitos")
    @NotBlank(message = "El RUC no puede estar vacío")
    @Pattern(regexp = "\\d+", message = "El RUC debe contener solo dígitos")
    private String agtRuc;

    @Column(name = "agt_razonsocial", length = 45)
    private String agtRazonsocial;

    @Column(name = "az_fechanacimiento")
    private LocalDate azFechanacimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idsolicitudagente")
    private Solicitudagente idSolicitudAgente;

    @Column(name = "agt_codigojurisdiccion", length = 45)
    @Size(min = 4, max = 6, message = "El código de jurisdicción debe tener entre 4 a 6 dígitos")
    @NotBlank(message = "El código de jurisdicción no puede estar vacío")
    @Pattern(regexp = "\\d+", message = "El código de jurisdicción debe contener solo dígitos")
    private String agtCodigojurisdiccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idagente")
    private Usuario idAgente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idadminzonal")
    private Usuario idAdminZonal;

    @Lob
    @Column(name = "foto")
    private byte[] foto;


    @Column(name = "telefono", nullable = false, length = 45)
    @Size(min = 9, max = 9, message = "El teléfono debe tener exactamente 9 dígitos")
    @NotBlank(message = "Debe ingresar un número de teléfono")
    @Pattern(regexp = "\\d+", message = "El teléfono debe contener solo dígitos")
    private String telefono;

    @Column(name = "dni", nullable = false, length = 45)
    @Size(min = 8, max = 8, message = "El dni debe tener exactamente 8 dígitos")
    @NotBlank(message = "Debe ingresar un número de dni")
    @Pattern(regexp = "\\d+", message = "El dni debe contener solo dígitos")
    private String dni;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idzona", nullable = false)
    private Zona zona;

    @Column(name = "apellidopaterno", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar un apellido paterno")
    @Size(max = 20, message = "El apellido paterno no puede tener más de 20 caracteres")
    private String apellidoPaterno;

    @Column(name = "apellidomaterno", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar un apellido materno")
    @Size(max = 20, message = "El apellido materno no puede tener más de 20 caracteres")
    private String apellidoMaterno;

    @Column(name = "contrasena", nullable = false, length = 45)
    private String contrasena;

    @Column(name = "activo")
    private Integer activo;

    @Column(name = "u_cantImportaciones", nullable = false, length = 45)
    @Pattern(regexp = "\\d+", message = "Las importaciones deben contener solo números positivos")
    private String uCantimportaciones;

}