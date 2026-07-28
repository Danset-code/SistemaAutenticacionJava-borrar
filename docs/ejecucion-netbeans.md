# Ejecución en NetBeans

## Backend

1. Abrir `backend` como proyecto Maven.
2. Ejecutar `src/main/java/com/autenticacion/Main.java`.
3. Verificar `http://localhost:8080`.

## Frontend

1. Abrir `frontend` como proyecto Maven Web Application.
2. Configurar Apache Tomcat en NetBeans.
3. Asociar Tomcat al proyecto.
4. Ejecutar el proyecto con Run.
5. Abrir la URL indicada por NetBeans `http://localhost:8081`.

El frontend no tiene `Main.java` y no usa `exec-maven-plugin`.
Es una aplicación web WAR desplegada en Tomcat.

El backend sí utiliza `Main.java` para iniciar Spring Boot.
