package com.example.gtics.repository;

import com.example.gtics.entity.Estadoorden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoOrdenRepository extends JpaRepository<Estadoorden,Integer> {
}
