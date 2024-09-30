package com.example.gtics.dto;

import java.time.LocalDate;

public interface OrdenCarritoDto{


    LocalDate getFechaOrden();
    double getMontoTotal();
    String getEstadoOrden();
    String getControlOrden();
    Integer getIdOrden();
    String getNombre();
    String getApellidoPaterno();
}