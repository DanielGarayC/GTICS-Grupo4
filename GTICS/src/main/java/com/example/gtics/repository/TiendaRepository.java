package com.example.gtics.repository;

import com.example.gtics.entity.Orden;
import com.example.gtics.entity.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiendaRepository extends JpaRepository<Tienda, Integer> {
}
