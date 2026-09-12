# Examen Final de Automatización de Pruebas

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