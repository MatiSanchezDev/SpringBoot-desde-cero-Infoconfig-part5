# Ferretería Antillana — API REST con Spring Boot + Spring Security

Proyecto desarrollado como parte del curso **"Spring Boot desde Cero"** de Infoconfig (Partes 5 y 6).
Es una API REST completa para gestionar el inventario de artículos de una ferretería, con frontend integrado servido por el mismo servidor y **autenticación con Spring Security, roles y contraseñas cifradas con BCrypt**.

---

## Tabla de Contenidos

- [Descripción general](#descripción-general)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuración de la base de datos](#configuración-de-la-base-de-datos)
- [Arquitectura por capas](#arquitectura-por-capas)
  - [1. Entidad (Entity)](#1-entidad-entity)
  - [2. DTO (Data Transfer Object)](#2-dto-data-transfer-object)
  - [3. Mapper](#3-mapper)
  - [4. Repositorio (Repository)](#4-repositorio-repository)
  - [5. Servicio (Service)](#5-servicio-service)
  - [6. Controlador (Controller)](#6-controlador-controller)
  - [7. Clase principal (Main)](#7-clase-principal-main)
- [Endpoints de la API](#endpoints-de-la-api)
- [Seguridad con Spring Security](#seguridad-con-spring-security)
  - [Entidad Usuario](#entidad-usuario)
  - [Repositorio de Usuarios](#repositorio-de-usuarios)
  - [UsuarioService — UserDetailsService](#usuarioservice--userdetailsservice)
  - [SecurityConfig](#securityconfig)
  - [GeneraPass — utilidad BCrypt](#generapass--utilidad-bcrypt)
- [Conceptos clave de Spring Boot aplicados](#conceptos-clave-de-spring-boot-aplicados)
- [Frontend estático integrado](#frontend-estático-integrado)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)

---

## Descripción general

Esta aplicación expone una API REST que permite realizar operaciones **CRUD** (Crear, Leer, Actualizar, Eliminar) sobre artículos de una ferretería almacenados en una base de datos MySQL. El frontend es una página HTML servida directamente por Spring Boot desde la carpeta `static`, que consume la API sin necesidad de un servidor aparte.

---

## Tecnologías utilizadas

| Tecnología              | Versión   | Rol                                          |
|-------------------------|-----------|----------------------------------------------|
| Java                    | 25        | Lenguaje de programación                     |
| Spring Boot             | 4.0.6     | Framework principal                          |
| Spring Web MVC          | (incluido)| Manejo de peticiones HTTP / REST             |
| Spring Data JPA         | (incluido)| Acceso a datos con Hibernate                 |
| Spring Security         | (incluido)| Autenticación, autorización y cifrado        |
| BCryptPasswordEncoder   | (incluido)| Cifrado seguro de contraseñas                |
| MySQL Connector/J       | (incluido)| Driver JDBC para MySQL                       |
| Lombok                  | (incluido)| Reducción de código boilerplate en entidades |
| Spring Boot DevTools    | (incluido)| Recarga automática en desarrollo             |
| Maven                   | (incluido)| Gestión de dependencias y build              |

---

## Estructura del proyecto

```
ferreteria/
├── src/
│   ├── main/
│   │   ├── java/com/hibernate/ferreteria/
│   │   │   ├── FerreteriaApplication.java        ← Clase principal / arranque
│   │   │   ├── Controller/
│   │   │   │   └── ArticuloController.java       ← Endpoints REST
│   │   │   ├── Service/
│   │   │   │   ├── ArticuloService.java          ← Lógica de negocio artículos
│   │   │   │   └── UsuarioService.java           ← Carga usuarios para Spring Security
│   │   │   ├── Repository/
│   │   │   │   ├── Repo_Articulos.java           ← Acceso a BD con JPA
│   │   │   │   └── Repo_usuario.java             ← Búsqueda de usuarios por nombre
│   │   │   ├── Entity/
│   │   │   │   ├── Articulos.java                ← Entidad JPA (tabla articulos)
│   │   │   │   └── Usuario.java                  ← Entidad JPA (tabla usuarios)
│   │   │   ├── DTOs/
│   │   │   │   └── ArticuloDTO.java              ← Objeto de transferencia de datos
│   │   │   ├── Mapper/
│   │   │   │   └── Articulo_Mapper.java          ← Conversión Entity <-> DTO
│   │   │   ├── Security/
│   │   │   │   └── SecurityConfig.java           ← Configuración de Spring Security
│   │   │   └── GeneraPass.java                   ← Utilidad para generar hashes BCrypt
│   │   └── resources/
│   │       ├── application.properties            ← Configuración de la app
│   │       └── static/
│   │           └── index.html                    ← Frontend servido por Spring Boot
│   └── test/
│       └── java/com/hibernate/ferreteria/
│           └── FerreteriaApplicationTests.java
└── pom.xml
```

---

## Configuración de la base de datos

El archivo `src/main/resources/application.properties` contiene toda la configuración de conexión:

```properties
spring.application.name=ferreteria
spring.datasource.url=jdbc:mysql://localhost:3306/db_ferreteria
spring.datasource.username=root
spring.datasource.password=admin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=true
```

- **`spring.datasource.url`**: dirección de la base de datos MySQL. El esquema debe llamarse `db_ferreteria`.
- **`spring.jpa.database-platform`**: le indica a Hibernate qué dialecto SQL usar para generar las queries correctas para MySQL.
- **`spring.jpa.show-sql=true`**: imprime en consola cada query SQL que ejecuta Hibernate, muy útil para depuración.

Las tablas necesarias en MySQL son:

```sql
CREATE DATABASE db_ferreteria;

USE db_ferreteria;

CREATE TABLE articulos (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_articulo VARCHAR(255),
    precio          DOUBLE,
    existencia      INT
);

CREATE TABLE usuarios (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario  VARCHAR(50) NOT NULL UNIQUE,
    rol      VARCHAR(60) NOT NULL,
    password VARCHAR(60) NOT NULL UNIQUE
);

-- Insertar un usuario ADMIN (contraseña: admin123 cifrada con BCrypt)
INSERT INTO usuarios (usuario, rol, password)
VALUES ('admin', 'ADMIN', '$2a$10$...');  -- usar GeneraPass.java para obtener el hash real
```

> **Nota:** Spring Boot con `spring.jpa.generate-ddl=true` crea las tablas automáticamente al arrancar si no existen.

---

## Arquitectura por capas

El proyecto sigue la arquitectura estándar de Spring Boot en capas, separando responsabilidades claramente:

```
HTTP Request
     |
[ Controller ]   <- Recibe la petición, delega al servicio
     |
[  Service  ]    <- Lógica de negocio, usa el repositorio y el mapper
     |
[ Repository ]   <- Acceso a la base de datos (Spring Data JPA)
     |
[  Entity   ]    <- Representa la tabla en la base de datos
     |
[  Mapper   ]    <- Convierte entre Entity y DTO (en ambas direcciones)
     |
[   DTO     ]    <- Lo que se expone/recibe en la API (sin exponer la entidad directa)
```

---

### 1. Entidad (Entity)

**Archivo:** `Entity/Articulos.java`

```java
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
```

**Anotaciones importantes:**

| Anotación | Propósito |
|---|---|
| `@Entity` | Le dice a JPA/Hibernate que esta clase mapea a una tabla en la BD |
| `@Table(name="articulos")` | Especifica el nombre exacto de la tabla |
| `@Id` | Indica el campo que es clave primaria |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | El `id` se genera automáticamente con auto-increment de la BD |
| `@Column(name = "...")` | Mapea el campo Java con el nombre de columna en la tabla |
| `@Data` | Lombok genera getters, setters, `toString`, `equals` y `hashCode` |
| `@NoArgsConstructor` | Lombok genera constructor vacío (requerido por JPA) |
| `@AllArgsConstructor` | Lombok genera constructor con todos los campos |

> **Por qué no se expone la entidad directamente en la API:** Las entidades están ligadas al esquema de la base de datos. Si la API devolviera entidades directamente, cualquier cambio en la BD impactaría inmediatamente la API. El DTO actúa como contrato estable entre la API y sus clientes.

---

### 2. DTO (Data Transfer Object)

**Archivo:** `DTOs/ArticuloDTO.java`

El DTO es un objeto simple sin lógica de negocio cuyo único propósito es transportar datos entre la capa de servicio y el controlador (y de ahí al cliente HTTP).

En este proyecto el DTO tiene los mismos campos que la entidad, pero en proyectos reales puede tener campos calculados, omitir campos sensibles (contraseñas, tokens internos), o aplanar relaciones complejas.

```java
public class ArticuloDTO {
    private Long id;
    private String nombre_articulo;
    private double precio;
    private Integer existencia;

    // Constructor, getters, setters y toString...
}
```

A diferencia de la entidad, el DTO no tiene anotaciones de JPA. Es un POJO (Plain Old Java Object) puro.

---

### 3. Mapper

**Archivo:** `Mapper/Articulo_Mapper.java`

El mapper es una clase utilitaria con métodos estáticos que se encargan de convertir entre `Articulos` (entidad) y `ArticuloDTO`.

```java
public class Articulo_Mapper {

    public static ArticuloDTO toDTO(Articulos articulo) {
        return new ArticuloDTO(
                articulo.getId(),
                articulo.getNombre_articulo(),
                articulo.getPrecio(),
                articulo.getExistencia()
        );
    }

    public static Articulos toEntity(ArticuloDTO dto) {
        Articulos articulo = new Articulos();
        articulo.setId(dto.getId());
        articulo.setNombre_articulo(dto.getNombre_articulo());
        articulo.setPrecio(dto.getPrecio());
        articulo.setExistencia(dto.getExistencia());
        return articulo;
    }
}
```

- **`toDTO`**: se usa cuando se va a **devolver** datos al cliente (la BD devuelve una entidad, el mapper la convierte a DTO).
- **`toEntity`**: se usa cuando se va a **guardar** datos (el cliente envía un DTO, el mapper lo convierte a entidad para persistirla).

Esta separación evita que el servicio tenga lógica de mapeo mezclada con lógica de negocio.

---

### 4. Repositorio (Repository)

**Archivo:** `Repository/Repo_Articulos.java`

```java
public interface Repo_Articulos extends JpaRepository<Articulos, Long> {
}
```

Extender `JpaRepository<T, ID>` es todo lo que se necesita. Spring Data JPA genera automáticamente en tiempo de ejecución una implementación completa con los siguientes métodos disponibles (entre otros):

| Método heredado | Equivalente SQL |
|---|---|
| `findAll()` | `SELECT * FROM articulos` |
| `findById(id)` | `SELECT * FROM articulos WHERE id = ?` |
| `save(entidad)` | `INSERT` o `UPDATE` según si tiene `id` o no |
| `deleteById(id)` | `DELETE FROM articulos WHERE id = ?` |
| `existsById(id)` | `SELECT COUNT(*) > 0 WHERE id = ?` |

Los dos parámetros genéricos son: la **clase de la entidad** (`Articulos`) y el **tipo del id** (`Long`).

---

### 5. Servicio (Service)

**Archivo:** `Service/ArticuloService.java`

La capa de servicio contiene la lógica de negocio y actúa de intermediaria entre el controlador y el repositorio.

```java
@Service
public class ArticuloService {

    @Autowired
    private Repo_Articulos repo;

    // ...métodos CRUD
}
```

**`@Service`**: marca la clase como un componente de servicio. Spring la detecta y la registra en su contenedor de dependencias (IoC Container).

**`@Autowired`**: inyección de dependencias. Spring crea automáticamente una instancia del repositorio y la inyecta aquí, sin necesidad de hacer `new Repo_Articulos()` manualmente.

#### Método listar — uso de Streams

```java
public List<ArticuloDTO> serv_consulta() {
    return repo.findAll().stream()
               .map(Articulo_Mapper::toDTO)
               .collect(Collectors.toList());
}
```

- `repo.findAll()` devuelve una `List<Articulos>`.
- `.stream()` convierte la lista en un flujo de datos para procesarlo funcionalmente.
- `.map(Articulo_Mapper::toDTO)` aplica el mapper a cada elemento (`::` es referencia a método estático).
- `.collect(Collectors.toList())` materializa el stream de vuelta en una lista.

#### Método buscar por ID — uso de Optional

```java
public ArticuloDTO serv_buscaId(Long id) {
    Articulos articuloPorId = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Articulo con id: " + id + " no encontrado."));
    return Articulo_Mapper.toDTO(articuloPorId);
}
```

`findById()` devuelve un `Optional<Articulos>`, no una entidad directamente. Esto evita el `NullPointerException` cuando el registro no existe.
- `.orElseThrow(...)` lanza una excepción si el Optional está vacío (el artículo no se encontró en la BD).

#### Método actualizar — verificación con Optional

```java
public ArticuloDTO serv_actualiza(Long id, ArticuloDTO dto) {
    Optional<Articulos> existe = repo.findById(id);

    if (existe.isPresent()) {
        Articulos articulo = existe.get();
        articulo.setNombre_articulo(dto.getNombre_articulo());
        articulo.setPrecio(dto.getPrecio());
        articulo.setExistencia(dto.getExistencia());

        Articulos actualizado = repo.save(articulo);
        return Articulo_Mapper.toDTO(actualizado);
    } else {
        throw new RuntimeException("Articulo no encontrado con id: " + id);
    }
}
```

El patrón es: buscar la entidad existente → modificar sus campos con los datos del DTO → llamar `repo.save()` (que hace UPDATE porque la entidad ya tiene id).

---

### 6. Controlador (Controller)

**Archivo:** `Controller/ArticuloController.java`

```java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/articulos")
public class ArticuloController {

    @Autowired
    private ArticuloService servicio;

    @GetMapping
    public List<ArticuloDTO> listar() { ... }

    @GetMapping("/{id}")
    public ArticuloDTO duscaId(@PathVariable Long id) { ... }

    @PostMapping
    public ArticuloDTO insertarArticulo(@RequestBody ArticuloDTO dto) { ... }

    @PutMapping("/{id}")
    public ArticuloDTO actualizaArticulo(@PathVariable Long id, @RequestBody ArticuloDTO dto) { ... }

    @DeleteMapping("/{id}")
    public String borrarArticulos(@PathVariable Long id) { ... }
}
```

**Anotaciones del controlador:**

| Anotación | Propósito |
|---|---|
| `@RestController` | Combinación de `@Controller` + `@ResponseBody`. Indica que todos los métodos devuelven datos serializados a JSON directamente en el cuerpo de la respuesta HTTP |
| `@RequestMapping("/api/articulos")` | Define la URL base para todos los endpoints de este controlador |
| `@CrossOrigin(origins = "*")` | Habilita CORS para cualquier origen. Permite que el frontend (aunque venga de otro dominio o puerto) pueda consumir la API |
| `@GetMapping` | Maneja peticiones HTTP GET |
| `@PostMapping` | Maneja peticiones HTTP POST |
| `@PutMapping("/{id}")` | Maneja peticiones HTTP PUT con variable en la URL |
| `@DeleteMapping("/{id}")` | Maneja peticiones HTTP DELETE con variable en la URL |
| `@PathVariable` | Extrae el valor de la variable `{id}` de la URL y lo inyecta como parámetro |
| `@RequestBody` | Deserializa el JSON del cuerpo de la petición HTTP a un objeto Java (`ArticuloDTO`) |

---

### 7. Clase principal (Main)

**Archivo:** `FerreteriaApplication.java`

```java
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
```

- **`@SpringBootApplication`**: anotación compuesta que activa la configuración automática de Spring Boot (`@EnableAutoConfiguration`), el escaneo de componentes (`@ComponentScan`) y la configuración de la aplicación.
- **`CommandLineRunner`**: interfaz que obliga a implementar el método `run()`. Ese método se ejecuta automáticamente **una sola vez al arrancar la aplicación**, después de que el contexto de Spring esté completamente cargado. Útil para verificar el estado inicial de la base de datos o sembrar datos de prueba.

---

## Endpoints de la API

Base URL: `http://localhost:8080/api/articulos`

| Método HTTP | URL                         | Descripción                        | Auth requerida         | Body                             |
|-------------|-----------------------------|------------------------------------|------------------------|----------------------------------|
| `GET`       | `/api/articulos`            | Obtiene todos los artículos        | ADMIN o USER           | —                                |
| `GET`       | `/api/articulos/{id}`       | Obtiene un artículo por su ID      | ADMIN o USER           | —                                |
| `POST`      | `/api/articulos`            | Crea un nuevo artículo             | ADMIN o USER           | `{ nombre, precio, existencia }` |
| `PUT`       | `/api/articulos/{id}`       | Actualiza un artículo existente    | ADMIN o USER           | `{ nombre, precio, existencia }` |
| `DELETE`    | `/api/articulos/{id}`       | Elimina un artículo por su ID      | ADMIN o USER           | —                                |

> Todos los endpoints de `/api/articulos/**` requieren autenticación HTTP Basic o formulario de login. Enviar `Authorization: Basic base64(usuario:password)` en el header.

### Ejemplo de body para POST / PUT

```json
{
  "nombre_articulo": "Martillo carpintero",
  "precio": 1250.50,
  "existencia": 30
}
```

### Ejemplo de respuesta exitosa (GET o POST)

```json
{
  "id": 1,
  "nombre_articulo": "Martillo carpintero",
  "precio": 1250.50,
  "existencia": 30
}
```

---

## Seguridad con Spring Security

En la última parte del curso se incorporó **Spring Security** para proteger los endpoints de la API con autenticación basada en roles. Los usuarios se almacenan en la base de datos con contraseñas cifradas usando **BCrypt**.

### Flujo de autenticación

```
Petición HTTP
     |
[ Spring Security Filter Chain ]
     |
  ¿Tiene credenciales?
     |-- NO → 401 Unauthorized / redirige a login
     |-- SÍ  → UsuarioService.loadUserByUsername()
                    |
               Repo_usuario.findByUsuario()  ← consulta BD
                    |
               Verifica password con BCrypt
                    |
               Asigna rol (ADMIN / USER)
                    |
              ¿Tiene el rol requerido?
                    |-- SÍ → acceso permitido al endpoint
                    |-- NO → 403 Forbidden
```

---

### Entidad Usuario

**Archivo:** `Entity/Usuario.java`

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 60)
    private String rol;          // "ADMIN" o "USER"

    @Column(nullable = false, unique = true, length = 60)
    private String password;     // hash BCrypt, nunca texto plano

    // Getters y setters...
}
```

| Campo | Descripción |
|---|---|
| `usuario` | Nombre de usuario único para login |
| `rol` | Rol del usuario: `ADMIN` o `USER`. Spring Security antepone `ROLE_` al verificar |
| `password` | Hash BCrypt de la contraseña (60 caracteres fijos) |

---

### Repositorio de Usuarios

**Archivo:** `Repository/Repo_usuario.java`

```java
public interface Repo_usuario extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
}
```

Spring Data JPA genera automáticamente la query `SELECT * FROM usuarios WHERE usuario = ?` a partir del nombre del método `findByUsuario`. Devuelve `Optional` para manejar el caso de usuario no encontrado sin `NullPointerException`.

---

### UsuarioService — UserDetailsService

**Archivo:** `Service/UsuarioService.java`

```java
@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private Repo_usuario repoUsuario;

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario)
            throws UsernameNotFoundException {
        var usuario = repoUsuario.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + nombreUsuario));
        return new User(usuario.getUsuario(), usuario.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol())));
    }
}
```

**Puntos clave:**
- Implementa la interfaz `UserDetailsService` de Spring Security — es el contrato que Spring usa para cargar usuarios desde cualquier fuente (BD, LDAP, memoria, etc.).
- El método `loadUserByUsername()` es llamado automáticamente por Spring Security cuando alguien intenta autenticarse.
- `SimpleGrantedAuthority("ROLE_ADMIN")` define el rol del usuario en el contexto de seguridad. Spring Security espera el prefijo `ROLE_`.

---

### SecurityConfig

**Archivo:** `Security/SecurityConfig.java`

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioService userService;

    public SecurityConfig(UsuarioService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder codificaPass() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager autenticacion(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityChain(HttpSecurity http, AuthenticationManager authManager)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/articulos/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated())
            .authenticationManager(authManager)
            .userDetailsService(userService)
            .formLogin(form -> form.permitAll())
            .httpBasic(basic -> {});

        return http.build();
    }
}
```

**Qué configura cada bean:**

| Bean | Propósito |
|---|---|
| `BCryptPasswordEncoder` | Encoder para verificar contraseñas hasheadas. Spring lo inyecta automáticamente al autenticar |
| `AuthenticationManager` | Orquesta el proceso de autenticación. Spring lo construye usando el `UserDetailsService` registrado |
| `SecurityFilterChain` | Define las reglas de acceso para cada URL y los mecanismos de login |

**Reglas de acceso configuradas:**

| URL | Regla |
|---|---|
| `/api/auth/**` | Pública — sin autenticación |
| `/api/articulos/**` | Requiere rol `ADMIN` o `USER` |
| Cualquier otra | Requiere autenticación |

**`csrf.disable()`**: CSRF (Cross-Site Request Forgery) se deshabilita porque la API REST usa tokens o Basic Auth, no cookies de sesión. En APIs REST es la práctica habitual.

---

### GeneraPass — utilidad BCrypt

**Archivo:** `GeneraPass.java`

```java
public class GeneraPass {
    static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin123"));
    }
}
```

Herramienta de desarrollo para generar el hash BCrypt de una contraseña en texto plano. El resultado se copia y se inserta directamente en la base de datos. **Nunca se almacenan contraseñas en texto plano.**

BCrypt genera un hash diferente cada vez (por el salt aleatorio), pero la verificación siempre funciona correctamente porque el salt está embebido en el propio hash.

---

## Conceptos clave de Spring Boot aplicados

### Inyección de Dependencias (IoC)
Spring gestiona la creación y el ciclo de vida de los objetos (beans). En lugar de crear manualmente `new ArticuloService()`, se declara `@Autowired` y Spring inyecta la instancia correspondiente. Esto desacopla las capas y facilita el testing.

### Spring Data JPA + Hibernate
Spring Data JPA es una abstracción sobre Hibernate (que es la implementación de JPA). Hibernate traduce las operaciones del repositorio (`findAll`, `save`, etc.) a queries SQL reales contra la base de datos MySQL. Con `spring.jpa.show-sql=true` se puede ver exactamente qué SQL genera.

### Patrón DTO
Exponer entidades de JPA directamente en la API es una mala práctica porque:
1. Acopla el esquema de la BD con el contrato de la API.
2. Puede exponer datos sensibles.
3. Genera problemas con relaciones bidireccionales (referencias circulares al serializar a JSON).

El DTO es el contrato público de la API. La entidad es un detalle de implementación interna.

### Serialización automática a JSON
`@RestController` + Jackson (incluido automáticamente por Spring Boot) convierte los objetos Java a JSON y viceversa de forma transparente. Cuando un método devuelve un objeto Java, Spring lo serializa a JSON. Cuando llega un `@RequestBody`, Spring lo deserializa de JSON a Java. No se necesita configuración adicional.

### Archivos estáticos
Spring Boot sirve automáticamente los archivos ubicados en `src/main/resources/static/` bajo la URL raíz. Por eso el `index.html` es accesible en `http://localhost:8080/` sin ninguna configuración adicional.

### Optional en Java
`JpaRepository.findById()` devuelve `Optional<T>` en lugar de la entidad directamente. `Optional` es un contenedor que puede o no contener un valor, eliminando los `NullPointerException`. Se puede usar `.orElseThrow()` para lanzar una excepción controlada o `.isPresent()` para verificar si el valor existe antes de usarlo.

### Java Streams
La API de Streams de Java permite procesar colecciones de forma declarativa y funcional. En este proyecto se usa para convertir una lista de entidades a una lista de DTOs en una sola expresión:
```java
repo.findAll().stream()
    .map(Articulo_Mapper::toDTO)
    .collect(Collectors.toList());
```

---

## Frontend estático integrado

El archivo `src/main/resources/static/index.html` es servido directamente por Spring Boot y no requiere ningún servidor adicional. Implementa una interfaz web para el sistema de inventario "Ferretería Antillana" con operaciones de listar, buscar, insertar, actualizar y eliminar artículos consumiendo la API REST.

Al iniciar la aplicación, el frontend está disponible en: **`http://localhost:8080`**

---

## Cómo ejecutar el proyecto

### Prerrequisitos
- Java 25+
- Maven
- MySQL 8+ corriendo en `localhost:3306`
- Base de datos `db_ferreteria` creada con la tabla `articulos`

### Pasos

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/MatiSanchezDev/SpringBoot-desde-cero-Infoconfig-part5.git
   cd SpringBoot-desde-cero-Infoconfig-part5
   ```

2. Configurar credenciales de MySQL en `src/main/resources/application.properties` si son distintas a `root/admin`.

3. Ejecutar con Maven:
   ```bash
   mvn spring-boot:run
   ```

4. Abrir en el navegador:
   - **Frontend:** `http://localhost:8080`
   - **API REST:** `http://localhost:8080/api/articulos`

---

## Partes anteriores del curso

| Parte | Tema |
|---|---|
| Parte 1 | Introducción a Spring Boot, estructura del proyecto, primer endpoint |
| Parte 2 | Spring Data JPA, entidades, repositorios, CRUD básico |
| Parte 3 | Relaciones entre entidades, consultas personalizadas, servicios |
| Parte 4 | Interceptors (HandlerInterceptor, WebMvcConfigurer) |
| Parte 5 | API REST completa con capas: Entity, DTO, Mapper, Repository, Service, Controller |
| **Parte 6** | **Spring Security: autenticación, roles, BCrypt, SecurityFilterChain, UserDetailsService** |
