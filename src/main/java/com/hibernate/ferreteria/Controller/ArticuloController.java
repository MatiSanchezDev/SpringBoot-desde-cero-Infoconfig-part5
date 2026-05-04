package com.hibernate.ferreteria.Controller;


import com.hibernate.ferreteria.DTOs.ArticuloDTO;
import com.hibernate.ferreteria.Service.ArticuloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/articulos")
public class ArticuloController {

    @Autowired
    private ArticuloService servicio;

    @GetMapping
    public List<ArticuloDTO> listar(){
        return servicio.serv_consulta();
    }

    @GetMapping("/{id}")
    public ArticuloDTO duscaId(@PathVariable Long id){
        return servicio.serv_buscaId(id);
    }

    @PostMapping
    public ArticuloDTO insertarArticulo (@RequestBody ArticuloDTO dto){
        return servicio.serv_insertar(dto);
    }

    @PutMapping("/{id}")
    public ArticuloDTO actualizaArticulo(@PathVariable Long id, @RequestBody ArticuloDTO dto){
        return servicio.serv_actualiza(id, dto);
    }

    @DeleteMapping("/{id}")
    public String borrarArticulos (@PathVariable Long id){
        return servicio.serv_elimina(id);
    }

}
