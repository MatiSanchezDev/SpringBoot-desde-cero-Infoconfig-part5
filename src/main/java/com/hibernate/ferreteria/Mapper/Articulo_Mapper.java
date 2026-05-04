package com.hibernate.ferreteria.Mapper;

import com.hibernate.ferreteria.DTOs.ArticuloDTO;
import com.hibernate.ferreteria.Entity.Articulos;

public class Articulo_Mapper {

    public static ArticuloDTO toDTO(Articulos articulo){
        return new ArticuloDTO(
                articulo.getId(),
                articulo.getNombre_articulo(),
                articulo.getPrecio(),
                articulo.getExistencia()
        );
    }

    public static Articulos toEntity(ArticuloDTO dto){
        Articulos articulo = new Articulos();
        articulo.setId(dto.getId());
        articulo.setNombre_articulo(dto.getNombre_articulo());
        articulo.setPrecio(dto.getPrecio());
        articulo.setExistencia(dto.getExistencia());
        return articulo;
    }
}
