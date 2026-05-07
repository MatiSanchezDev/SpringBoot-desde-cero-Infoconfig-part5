package com.hibernate.ferreteria.Entity;


import jakarta.persistence.*;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name= "usuarios")
public class Usuario {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 60)
    private String rol;

    @Column(nullable = false,unique = true, length = 60)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
