package com.hibernate.ferreteria.DTOs;

public class ArticuloDTO {
    private Long id;
    private String nombre_articulo;
    private double precio;
    private Integer existencia;

    public ArticuloDTO(Long id, String nombre_articulo, double precio, Integer existencia) {
        this.id = id;
        this.nombre_articulo = nombre_articulo;
        this.precio = precio;
        this.existencia = existencia;
    }

    public Integer getExistencia() {
        return existencia;
    }

    public void setExistencia(Integer existencia) {
        this.existencia = existencia;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getNombre_articulo() {
        return nombre_articulo;
    }

    public void setNombre_articulo(String nombre_articulo) {
        this.nombre_articulo = nombre_articulo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // Solo visible en consola
    @Override
    public String toString() {
        return "ArticuloDTO{" +
                "id=" + id +
                ", nombre_articulo='" + nombre_articulo + '\'' +
                ", precio=" + precio +
                ", existencia=" + existencia +
                '}';
    }
}
