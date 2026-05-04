package com.hibernate.ferreteria.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="articulos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Articulos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "nombre_articulo")
    private String nombre_articulo;
    @Column(name = "precio")
    private Double precio;
    @Column(name = "existencia")
    private Integer existencia;

}
