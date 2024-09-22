package com.example.gtics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class ProductoHasCarritocompraId implements Serializable {
    private static final long serialVersionUID = -4940726392111327519L;
    @Column(name = "idProducto", nullable = false)
    private Integer idProducto;

    @Column(name = "idCarritoCompra", nullable = false)
    private Integer idCarritoCompra;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ProductoHasCarritocompraId entity = (ProductoHasCarritocompraId) o;
        return Objects.equals(this.idCarritoCompra, entity.idCarritoCompra) &&
                Objects.equals(this.idProducto, entity.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCarritoCompra, idProducto);
    }

}