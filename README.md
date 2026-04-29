# Spring Boot desde Cero — Parte 4: Interceptors

Proyecto del curso **Spring Boot desde Cero (Infoconfig)** — Parte 4.
Stack: Spring Boot 4.x · Java 25 · Spring Data JPA · MySQL · Maven

---

## Tema central: Interceptors

### ¿Qué es un Interceptor?

Un **Interceptor** en Spring MVC es un componente que permite interceptar las peticiones HTTP **antes** de que lleguen al controlador, **después** de que el controlador las procese, o **al finalizar** la respuesta. Es similar a un filtro (Servlet Filter), pero opera a nivel del framework Spring MVC y tiene acceso al contexto del handler.

```
Cliente → DispatcherServlet → [Interceptor] → Controller → [Interceptor] → Cliente
```

---

## Ciclo de vida de un Interceptor

Un interceptor implementa la interfaz `HandlerInterceptor`, que expone tres métodos:

| Método | Cuándo se ejecuta | Retorno |
|---|---|---|
| `preHandle` | Antes del controller | `boolean` — `true` continúa, `false` corta la cadena |
| `postHandle` | Después del controller, antes de renderizar la vista | `void` |
| `afterCompletion` | Al finalizar toda la request (siempre se ejecuta) | `void` |

---

## Implementación paso a paso

### 1. Crear el Interceptor

```java
package com.hibernate.ferreteria.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        System.out.println("[PRE] " + request.getMethod() + " " + request.getRequestURI());
        return true; // true = continuar; false = bloquear la request
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        System.out.println("[POST] Controlador ejecutado, status: " + response.getStatus());
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        System.out.println("[AFTER] Request completada");
    }
}
```

### 2. Registrar el Interceptor

El interceptor se registra sobreescribiendo `addInterceptors` en una clase que implemente `WebMvcConfigurer`:

```java
package com.hibernate.ferreteria.config;

import com.hibernate.ferreteria.interceptors.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")           // aplica a todas las rutas
                .excludePathPatterns("/health");  // excluye rutas específicas
    }
}
```

---

## Casos de uso comunes

### Interceptor de timing (medir duración de requests)

```java
@Component
public class TimingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        long start = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - start;
        System.out.println("Duración de " + request.getRequestURI() + ": " + duration + "ms");
    }
}
```

### Interceptor de autenticación por token

```java
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false; // corta la cadena, no llega al controller
        }

        return true;
    }
}
```

---

## Diferencia entre Interceptor y Filter

| Característica | Servlet Filter | Spring Interceptor |
|---|---|---|
| Nivel | Servlet API (javax/jakarta) | Spring MVC |
| Acceso al handler | No | Sí (`Object handler`) |
| Acceso al contexto Spring | Limitado | Total |
| Dónde se registra | `FilterRegistrationBean` | `WebMvcConfigurer` |
| Se ejecuta en requests estáticas | Sí | No (solo rutas mapeadas) |
| Orden | Configurable con `@Order` | Configurable en el registry |

**Regla general:** usá Filters para lógica genérica de bajo nivel (CORS, codificación). Usá Interceptors para lógica de negocio que necesita contexto Spring (auditoría, autenticación basada en roles, logging de negocio).

---

## Encadenamiento de múltiples Interceptors

Se pueden registrar varios interceptors. El orden de ejecución es:

```
preHandle(1) → preHandle(2) → Controller → postHandle(2) → postHandle(1) → afterCompletion(2) → afterCompletion(1)
```

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loggingInterceptor).addPathPatterns("/**").order(1);
    registry.addInterceptor(authInterceptor).addPathPatterns("/api/**").order(2);
    registry.addInterceptor(timingInterceptor).addPathPatterns("/**").order(3);
}
```

> Si `preHandle` de un interceptor retorna `false`, los interceptors anteriores en la cadena sí ejecutan `afterCompletion`, pero los siguientes no se ejecutan.

---

## Estructura del proyecto

```
src/
└── main/
    └── java/com/hibernate/ferreteria/
        ├── FerreteriaApplication.java
        ├── config/
        │   └── WebConfig.java          # registra los interceptors
        └── interceptors/
            ├── LoggingInterceptor.java
            ├── TimingInterceptor.java
            └── AuthInterceptor.java
```

---

## Dependencias (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## Conceptos clave del curso cubiertos en esta parte

- `HandlerInterceptor` — interfaz base para todos los interceptors
- `preHandle` / `postHandle` / `afterCompletion` — ciclo de vida completo
- `WebMvcConfigurer` — punto de extensión de Spring MVC
- `InterceptorRegistry` — registro y configuración de path patterns
- `addPathPatterns` / `excludePathPatterns` — control granular de rutas
- Encadenamiento y orden de múltiples interceptors
- Diferencia conceptual entre Filter y Interceptor

---

## Partes anteriores del curso

| Parte | Tema |
|---|---|
| Parte 1 | Introducción a Spring Boot, estructura del proyecto, primer endpoint |
| Parte 2 | Spring Data JPA, entidades, repositorios, CRUD básico |
| Parte 3 | Relaciones entre entidades, consultas personalizadas, servicios |
| **Parte 4** | **Interceptors (este proyecto)** |
