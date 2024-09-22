package com.example.gtics;

import com.example.gtics.entity.Carritocompra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "producto_has_carritocompra")
public class ProductoHasCarritocompra {
    @EmbeddedId
    private ProductoHasCarritocompraId id;

    @MapsId("idProducto")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idProducto", nullable = false)
    private Producto idProducto;

    @MapsId("idCarritoCompra")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idCarritoCompra", nullable = false)
    private Carritocompra idCarritoCompra;

}