package com.example.gtics.repository;

import com.example.gtics.entity.Carritocompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoCompraRepository extends JpaRepository<Carritocompra,Integer> {
}
