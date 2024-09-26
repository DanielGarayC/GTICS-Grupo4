package com.example.gtics.repository;

import com.example.gtics.entity.Categoria;
import com.example.gtics.entity.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Integer> {

}
