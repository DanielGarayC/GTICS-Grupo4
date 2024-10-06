package com.example.gtics.repository;

import com.example.gtics.entity.Fotosresena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotosResenaRepository extends JpaRepository<Fotosresena,Integer> {
}
