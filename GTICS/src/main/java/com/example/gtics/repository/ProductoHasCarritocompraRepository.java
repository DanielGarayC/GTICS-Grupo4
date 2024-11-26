package com.example.gtics.repository;

import com.example.gtics.dto.ProductosCarritoDto;
import com.example.gtics.entity.Carritocompra;
import com.example.gtics.entity.ProductoHasCarritocompra;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoHasCarritocompraRepository extends JpaRepository<ProductoHasCarritocompra,Integer> {

    @Transactional
    void deleteById_IdCarritoCompraAndId_IdProducto(Integer idCarritoCompra, Integer idProducto);

    Optional<ProductoHasCarritocompra> findById_IdCarritoCompraAndId_IdProducto(Integer idCarritoCompra, Integer idProducto);

    @Query("SELECT p.idProducto.id AS idProducto, p.idProducto.nombreProducto AS nombreProducto, p.cantidadProducto AS cantidadProducto, " +
            "p.idProducto.precio AS precioUnidad, (p.cantidadProducto * p.idProducto.precio) AS precioTotalPorProducto, " +
            "p.idProducto.costoEnvio AS costoEnvio, f.foto AS foto " +
            "FROM ProductoHasCarritocompra p " +
            "LEFT JOIN Fotosproducto f ON p.idProducto.id = f.producto.id " +
            "WHERE p.idCarritoCompra.id = :idCarrito")
    List<ProductosCarritoDto> findProductosPorCarrito(@Param("idCarrito") Integer idCarrito);


    int countById_IdCarritoCompra(Integer id);
}
