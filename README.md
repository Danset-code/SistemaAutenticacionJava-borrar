# SistemaAutenticacionJava

Proyecto académico con dos aplicaciones independientes:

- frontend: Maven Web Application (WAR), HTML/CSS/JavaScript.
- backend: Spring Boot REST API.
- database: script MySQL.

## Ejecución

1. Ejecutar `database/database.sql` en MySQL.
2. Configurar la contraseña de MySQL en `backend/src/main/resources/application.properties`.
3. Abrir `backend` en NetBeans y ejecutar `Main.java`.
4. Abrir `frontend` en NetBeans como proyecto Maven Web Application.
5. Configurar Apache Tomcat para el frontend.
6. Ejecutar el frontend.
7. Consumir el backend desde el frontend mediante HTTP y JSON.

Backend:
http://localhost:8080

Frontend:
http://localhost:8081/sistema-autenticacion-frontend/

## Endpoints

POST /api/auth/register
POST /api/auth/login

## Git

git init
git add .
git commit -m "Inicializar proyecto"
git add .
git commit -m "Agregar backend Spring Boot"
git add .
git commit -m "Agregar frontend web Maven"
git branch -M main
git remote add origin URL_DEL_REPOSITORIO
git push -u origin main
