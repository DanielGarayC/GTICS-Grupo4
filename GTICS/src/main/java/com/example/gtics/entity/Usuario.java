package com.example.gtics.entity;

import com.example.gtics.ValidationGroup.CustomAnnotations.UniqueEmail;
import com.example.gtics.ValidationGroup.RegistroUsuarioValidationGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.example.gtics.ValidationGroup.AdminZonalValidationGroup;
import com.example.gtics.ValidationGroup.AgenteValidationGroup;
import com.example.gtics.ValidationGroup.UsuarioFinalValidationGroup;
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
    @NotBlank(message = "Debe ingresar un nombre", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Size(max = 40, message = "El nombre no puede tener más de 40 caracteres", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]*$", message = "El nombre solo puede contener letras, números y espacios", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String nombre;


    @Column(name = "email", nullable = false, length = 45)
    @Email(message = "Debe ingresar un correo electrónico válido", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @NotBlank(message = "Debe ingresar un correo electrónico", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @UniqueEmail(groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String email;

    @Column(name = "direccion", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar una dirección", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iddistrito")
    @NotNull(message = "Debe seleccionar un distrito", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private Distrito distrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idrol", nullable = false)
    private Rol rol;

    @Column(name = "baneado")
    private boolean baneado;

    @Column(name = "agt_codigoaduana", length = 45)
    @NotBlank(message = "El código aduanero no puede estar vacío", groups = {AgenteValidationGroup.class})
    @Size(min = 6, max = 6, message = "El código aduanero debe tener exactamente 6 dígitos", groups = {AgenteValidationGroup.class})
    @Pattern(regexp = "\\d+", message = "El código aduanero debe contener solo dígitos", groups = {AgenteValidationGroup.class})
    private String agtCodigoaduana;

    @Column(name = "agt_ruc", length = 45)
    @NotBlank(message = "El RUC no puede estar vacío", groups = {AgenteValidationGroup.class})
    @Size(min = 10, max = 10, message = "El RUC debe tener exactamente 10 dígitos", groups = {AgenteValidationGroup.class})
    @Pattern(regexp = "\\d+", message = "El RUC debe contener solo dígitos", groups = {AgenteValidationGroup.class})
    private String agtRuc;

    @Column(name = "agt_razonsocial", length = 45)
    private String agtRazonsocial;

    @Column(name = "az_fechanacimiento")
    private LocalDate azFechanacimiento;


    @Column(name = "agt_codigojurisdiccion", length = 45)
    @Size(min = 4, max = 6, message = "El código de jurisdicción debe tener entre 4 a 6 dígitos", groups = {AgenteValidationGroup.class})
    @Pattern(regexp = "\\d+", message = "El código de jurisdicción debe contener solo dígitos", groups = {AgenteValidationGroup.class})
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
    @Size(min = 9, max = 9, message = "El teléfono debe tener exactamente 9 dígitos", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @NotBlank(message = "Debe ingresar un número de teléfono", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Pattern(regexp = "\\d+", message = "El teléfono debe contener solo dígitos", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String telefono;

    @Column(name = "dni", nullable = false, length = 45)
    @Size(min = 8, max = 8, message = "El dni debe tener exactamente 8 dígitos", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @NotBlank(message = "Debe ingresar un número de dni", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Pattern(regexp = "\\d+", message = "El dni debe contener solo dígitos", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String dni;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idzona", nullable = false)
    private Zona zona;

    @Column(name = "apellidopaterno", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar un apellido paterno", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Size(max = 20, message = "El apellido paterno no puede tener más de 20 caracteres", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]*$", message = "El apellido paterno solo puede contener letras, números y espacios", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String apellidoPaterno;

    @Column(name = "apellidomaterno", nullable = false, length = 45)
    @NotBlank(message = "Debe ingresar un apellido materno", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Size(max = 20, message = "El apellido materno no puede tener más de 20 caracteres", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]*$", message = "El apellido materno solo puede contener letras, números y espacios", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    private String apellidoMaterno;

    //@NotBlank(message = "Debe ingresar una contraseña", groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Pattern(regexp = "^(?=.*[0-9])(?=(.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]){2})(?=.*[a-zA-Z]).{8,16}$",
            message = "La contraseña debe contener al menos 1 número, 2 caracteres especiales, 1 letra y debe tener entre 8 y 16 caracteres.",
            groups = {AdminZonalValidationGroup.class, AgenteValidationGroup.class, UsuarioFinalValidationGroup.class, RegistroUsuarioValidationGroup.class})
    @Column(name = "contrasena", length = 45)
    private String contrasena;

    @Column(name = "activo")
    private Integer activo;

    @Column(name = "u_cantImportaciones", length = 45)
    private String uCantimportaciones;

    @Column(name = "direcciongenerada")
    private boolean direcciongenerada;

    @Column(name = "razonBaneado")
    private String razonBaneado;

}
