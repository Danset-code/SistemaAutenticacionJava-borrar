# Ejecutar el frontend Web desde NetBeans

Este frontend NO es una aplicación de escritorio y NO contiene un `main()` Java.

Es una **Maven Web Application** (`packaging=war`) que debe desplegarse en Apache Tomcat.

## Configuración recomendada

Como Spring Boot utiliza:

`http://localhost:8080`

configura Tomcat para utilizar:

`http://localhost:8081`

Así no hay conflicto de puertos.

## Pasos

1. En NetBeans: File > Open Project.
2. Seleccionar la carpeta `frontend`.
3. Clic derecho al proyecto > Properties > Run.
4. Seleccionar Apache Tomcat.
5. Configurar el servidor Tomcat con HTTP Port `8081`.
6. Ejecutar el proyecto.
7. La URL esperada es:

`http://localhost:8081/monitoreo/`

El frontend llama a Spring Boot en `http://localhost:8080/api` y al WebSocket en `ws://localhost:8080/ws/sensores`.
