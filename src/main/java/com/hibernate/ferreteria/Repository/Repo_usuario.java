package com.hibernate.ferreteria.Repository;


import com.hibernate.ferreteria.Entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Repo_usuario extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsuario(String usuario);

}
