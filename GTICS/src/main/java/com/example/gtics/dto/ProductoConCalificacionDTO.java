package com.example.gtics.dto;

import com.example.gtics.entity.Producto;

public class ProductoConCalificacionDTO {
    private Producto producto;
    private double averageRating;
    private long reviewCount;

    // Constructor
    public ProductoConCalificacionDTO(Producto producto, double averageRating, long reviewCount) {
        this.producto = producto;
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    // Getters y Setters
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }
}
