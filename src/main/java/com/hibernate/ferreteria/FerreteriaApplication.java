package com.hibernate.ferreteria;

import com.hibernate.ferreteria.Entity.Articulos;
import com.hibernate.ferreteria.Repository.Repo_Articulos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class FerreteriaApplication implements CommandLineRunner {

	@Autowired
	private Repo_Articulos repositorio;

	public static void main(String[] args) {
		SpringApplication.run(FerreteriaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Aplicación iniciada correctamente.");

		List<Articulos> articulos = repositorio.findAll();
		articulos.stream().forEach(System.out::println);
	}
}
