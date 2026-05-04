package com.hibernate.ferreteria.Service;

import com.hibernate.ferreteria.DTOs.ArticuloDTO;
import com.hibernate.ferreteria.Entity.Articulos;
import com.hibernate.ferreteria.Mapper.Articulo_Mapper;
import com.hibernate.ferreteria.Repository.Repo_Articulos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ArticuloService {

    @Autowired
    private Repo_Articulos repo;

    @Autowired
    public List<ArticuloDTO> serv_consulta(){
        return repo.findAll().stream().map(Articulo_Mapper::toDTO)
                .collect(Collectors.toList());
    }

    public ArticuloDTO serv_buscaId(Long id){
        Articulos articuloPorId = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Articulo con id: " + id +" no encontrado."));
        return Articulo_Mapper.toDTO(articuloPorId);

    }

    public ArticuloDTO serv_insertar (ArticuloDTO dto){
        Articulos articulo = Articulo_Mapper.toEntity(dto);
        Articulos insertado = repo.save(articulo);
        return Articulo_Mapper.toDTO(insertado);
    }

    public ArticuloDTO serv_actualiza (Long id, ArticuloDTO dto){
        Optional<Articulos> existe = repo.findById(id);

        if (existe.isPresent()){
            Articulos articulo = existe.get();

            articulo.setNombre_articulo(dto.getNombre_articulo());
            articulo.setPrecio(dto.getPrecio());
            articulo.setExistencia(dto.getExistencia());

            Articulos actualizado = repo.save(articulo);
        return Articulo_Mapper.toDTO(actualizado);
        }else {
            throw new RuntimeException("Articulo no encontrado con id: "+ id);
        }
    }

    public String serv_elimina (Long id){
        if (repo.existsById(id)){
            repo.deleteById(id);
            return "Articulo eliminado correctamente";
        }else{
            return "Articulo: " + id + " No encontrado";
        }

    }

}
