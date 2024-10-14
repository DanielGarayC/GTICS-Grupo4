package com.example.gtics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "direcciones")
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddirecciones")
    private Integer idDireccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private Usuario usuario;  // Relación con la tabla `usuario`

    @Column(name = "nombre_contacto", nullable = false, length = 100)
    private String nombreContacto;  // Nombre del contacto para la dirección

    @Column(name = "telefono", nullable = false, length = 15)
    private String telefono;  // Número de teléfono de contacto

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;  // Dirección física

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "iddistrito", nullable = false)
    private Distrito distrito;  // Relación con la tabla `distrito`

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idzona", nullable = false)
    private Zona zona;  // Relación con la tabla `zona`

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idetiqueta")
    private Etiqueta etiqueta;  // Relación con la tabla `etiquetas` (opcional)

    @Column(name = "ruc", length = 15)
    private String ruc;  // Número de RUC (opcional)

    @Column(name = "predeterminado", nullable = false)
    private Boolean predeterminado = false;  // Si es la dirección predeterminada o no

    // Campo transitorio para manejar etiquetas predefinidas (no se guarda en la base de datos)
    @Transient
    private String etiquetaPredefinida;  // "Casa", "Oficina", etc.

}
