package com.example.gtics.repository;

import com.example.gtics.entity.ControlOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlOrdenRepository extends JpaRepository<ControlOrden,Integer> {
}
