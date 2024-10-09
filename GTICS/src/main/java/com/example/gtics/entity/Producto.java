package com.example.gtics.entity;

import com.example.gtics.ValidationGroup.AdminZonalValidationGroup;
import com.example.gtics.ValidationGroup.AgenteValidationGroup;
import com.example.gtics.ValidationGroup.UsuarioFinalValidationGroup;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import com.example.gtics.ValidationGroup.InventarioProductosValidationGroup;
@Getter
@Setter
@Entity
@Table(name = "producto")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProducto", nullable = false)
    private Integer id;

    @Column(name = "nombreProducto", nullable = false, length = 45)
    @NotBlank(message = "Este campo es obligatorio", groups = {InventarioProductosValidationGroup.class})
    private String nombreProducto;

    @Column(name = "cantidadDisponible", nullable = false, length = 45)
    @Positive(message = "La cantidad disponible ingresada debe ser positiva", groups = {InventarioProductosValidationGroup.class})
    private Integer cantidadDisponible;

    @Lob
    @Column(name = "`descripcion`", nullable = false)
    @NotBlank(message = "Este es un campo obligatorio", groups = {InventarioProductosValidationGroup.class})
    private String descripcion;

    @Column(name = "precio", nullable = false, length = 45)
    @Digits(integer=6, fraction = 2, message = "El precio debe ser un número (máx. 2 decimales)", groups = {InventarioProductosValidationGroup.class})
    @NotNull(message = "Este es un campo obligatorio", groups = {InventarioProductosValidationGroup.class})
    @Positive(message = "El precio debe ser un número positivo", groups = {InventarioProductosValidationGroup.class})
    private Double precio;

    @Column(name = "costoEnvio", nullable = false, length = 45)
    @Digits(integer=6, fraction = 2, groups = {InventarioProductosValidationGroup.class})
    @NotNull(message = "Este es un campo obligatorio", groups = {InventarioProductosValidationGroup.class})
    @PositiveOrZero(message = "El costo de envío debe ser 0 o positivo", groups = {InventarioProductosValidationGroup.class})
    private Double costoEnvio;

    @Column(name = "modelo", length = 45)
    private String modelo;

    @Column(name = "color", length = 45)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCategoria", nullable = false)
    private Categoria idCategoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProveedor", nullable = false)
    private Proveedor idProveedor;


    @Column(name = "cantVentas", nullable = false, length = 45)
    private Integer cantVentas;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "idSubcategoria", nullable = true)
    private Subcategoria idSubcategoria;

    @Column(name = "borrado", nullable = false)
    private Integer borrado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zona_idZona", nullable = false)
    private Zona zona;

    @Column(name = "fechaArribo")
    private LocalDate fechaArribo;

    @Column(name = "disponibilidad", nullable = false)
    private String disponibilidad;

}