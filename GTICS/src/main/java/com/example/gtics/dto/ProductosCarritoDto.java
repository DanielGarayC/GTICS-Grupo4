package com.example.gtics.dto;

public interface ProductosCarritoDto {
    Integer getIdProducto();
    String getNombreProducto();
    Integer getCantidadProducto();
    Double getPrecioUnidad();
    Double getPrecioTotalPorProducto();
    String getUrlImagenProducto(); // Si no tienes este dato en la consulta, puede ser opcional
}