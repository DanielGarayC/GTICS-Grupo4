package com.example.gtics.dto;

public interface ProductosCarritoDto {
    Integer getIdProducto();
    String getNombreProducto();
    Integer getCantidadProducto();
    Double getPrecioUnidad();
    Double getPrecioTotalPorProducto();
    String getUrlImagenProducto();
   Double getCostoEnvio();
    Integer getCantidadDisponible(); // Añadido

}