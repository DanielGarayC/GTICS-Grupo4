package com.example.gtics.repository;

import com.example.gtics.entity.Distrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistritoRepository extends JpaRepository<Distrito, Integer> {

    List<Distrito> findByZona_Id(Integer zonaId); // Obtener distritos por zona

}
