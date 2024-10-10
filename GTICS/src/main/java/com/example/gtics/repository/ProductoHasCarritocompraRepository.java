package com.example.gtics.repository;

import com.example.gtics.entity.Carritocompra;
import com.example.gtics.entity.ProductoHasCarritocompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoHasCarritocompraRepository extends JpaRepository<ProductoHasCarritocompra,Integer> {
    Optional<ProductoHasCarritocompra> findById_IdCarritoCompraAndId_IdProducto(Integer idCarritoCompra, Integer idProducto);
    void deleteById_IdCarritoCompraAndId_IdProducto(Integer idCarritoCompra, Integer idProducto);

}
