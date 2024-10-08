package com.example.gtics.repository;

import com.example.gtics.entity.Carritocompra;
import com.example.gtics.entity.ProductoHasCarritocompra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoHasCarritocompraRepository extends JpaRepository<ProductoHasCarritocompra,Integer> {

}
