package com.example.gtics.dto;

public class EtiquetaForm {
    private String nombreEtiqueta;

    public EtiquetaForm() {
    }

    public EtiquetaForm(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }
}
