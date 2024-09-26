package com.example.gtics.repository;

import com.example.gtics.entity.Orden;
import com.example.gtics.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer>{
    @Query("SELECT p FROM Producto p WHERE p.borrado = 0")
    List<Producto> findAllActive();
}
