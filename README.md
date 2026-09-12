# Examen Final de Automatización de Pruebas
[![CI - Build y Pruebas](https://github.com/DavidSZagal/examen-final-automatizacion/actions/workflows/ci.yml/badge.svg)](https://github.com/DavidSZagal/examen-final-automatizacion/actions/workflows/ci.yml)

## Descripción

Proyecto académico desarrollado para demostrar la aplicación de automatización de pruebas, integración continua y despliegue continuo.

La solución corresponde a una API REST construida con Java, Spring Boot y Maven. El proyecto incorporará pruebas unitarias, pruebas de integración, pruebas de aceptación, un pipeline de CI y una estrategia de despliegue Blue-Green con rollback.

## Tecnologías utilizadas

- Java 17
- Spring Boot 4.1.1
- Maven
- JUnit
- Mockito
- Maven Surefire
- Maven Failsafe
- JaCoCo
- Git y GitHub

## Estrategia de ramas

El repositorio utiliza un flujo GitFlow simplificado:

- `main`: contiene las versiones estables del proyecto.
- `develop`: integra los cambios antes de publicarlos en `main`.
- `feature/*`: contiene el desarrollo de cada nueva funcionalidad o actividad.

Los cambios pasan desde una rama `feature` hacia `develop` y posteriormente desde `develop` hacia `main` mediante pull requests.

## Configuración de pruebas

- Maven Surefire ejecuta las pruebas unitarias.
- Maven Failsafe ejecuta las pruebas de integración.
- JaCoCo genera el reporte de cobertura del código.

## Ejecución local

- Pruebas unitarias: `mvn clean test`
- Verificación completa: `mvn clean verify`
- Verificación en Windows: `mvn.cmd clean verify`
- Reporte de cobertura: `target/site/jacoco/index.html`

## Autor

David Sandoval
## Actividad 2: Integración continua y pruebas automatizadas

### Funcionalidad implementada

Se desarrolló una API REST para administrar productos. La aplicación permite registrar productos, consultar el catálogo y reducir el stock disponible. Los datos se almacenan mediante Spring Data JPA y una base de datos H2.

### Endpoints disponibles

| Método | Endpoint | Descripción |
| --- | --- | --- |
| GET | `/api/productos` | Obtiene todos los productos registrados. |
| POST | `/api/productos` | Registra un producto validando nombre, precio y stock. |
| PATCH | `/api/productos/{id}/stock?cantidad=2` | Reduce el stock de un producto existente. |

### Estrategia de pruebas

El proyecto contiene los siguientes niveles de prueba:

- **Pruebas unitarias:** `ProductoServiceTest` utiliza JUnit y Mockito para comprobar la creación de productos, duplicados, reducción de stock y stock insuficiente.
- **Pruebas de integración:** `ProductoControllerIT` utiliza Spring Boot, MockMvc y H2 para comprobar los endpoints y su integración con la base de datos.
- **Prueba de contexto:** verifica que la aplicación Spring Boot pueda iniciar correctamente.

Actualmente se ejecutan:

- 5 pruebas unitarias y de contexto mediante Maven Surefire.
- 3 pruebas de integración mediante Maven Failsafe.
- 8 pruebas automatizadas en total.
- 0 fallos y 0 errores.

### Pipeline de integración continua

El workflow se encuentra en:

```text
.github/workflows/ci.yml
```

El pipeline se ejecuta automáticamente al realizar:

- Push en `main`, `develop` o ramas `feature/**`.
- Pull request hacia `main` o `develop`.
- Ejecución manual mediante `workflow_dispatch`.

El pipeline realiza las siguientes tareas:

1. Descarga el código del repositorio.
2. Configura Java 17 y la caché de Maven.
3. Compila el proyecto.
4. Ejecuta las pruebas unitarias.
5. Ejecuta las pruebas de integración.
6. Genera el reporte de cobertura JaCoCo.
7. Publica los reportes de prueba como artefactos.
8. Publica la aplicación Java compilada.

### Ejecución local

Para ejecutar todas las validaciones en Windows:

```bash
mvn.cmd clean verify
```

Para ejecutar solamente las pruebas unitarias:

```bash
mvn.cmd test
```

Los reportes quedan disponibles en:

```text
target/surefire-reports
target/failsafe-reports
target/site/jacoco/index.html
```