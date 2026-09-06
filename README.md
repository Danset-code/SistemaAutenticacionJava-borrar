# Sistema de Monitoreo de Cultivo Medicinal

Proyecto formativo para visualización de datos ambientales de un cultivo medicinal.

## Arquitectura

- **Backend:** Java 21 + Spring Boot 3.5 + Spring Data JPA + MySQL.
- **Frontend:** Maven Web Application (WAR) con HTML5, CSS y JavaScript, desplegable en Apache Tomcat desde NetBeans.
- **Comunicación en tiempo real:** WebSocket nativo en `/ws/sensores`.
- **API REST:** endpoints `/api/...`.
- **Datos ficticios:** el backend crea sensores, gráficos y lecturas iniciales y genera nuevas lecturas automáticamente cada 5 segundos.

## Puertos recomendados

Para evitar el conflicto que ocurre cuando Tomcat y Spring Boot intentan utilizar 8080:

- Backend Spring Boot: `http://localhost:8080`
- Frontend Tomcat: `http://localhost:8081/monitoreo/`

En NetBeans configura Tomcat para utilizar el puerto HTTP `8081`.

## Base de datos

Crear en MySQL:

```sql
CREATE DATABASE monitoreo_cultivo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Luego modificar `backend/src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD
```

El backend crea las tablas automáticamente con JPA.

## Ejecutar backend

Desde NetBeans:
1. Abrir la carpeta `backend` como proyecto Maven.
2. Ejecutar `Main.java` o `spring-boot:run`.
3. Verificar que Spring Boot escuche en `http://localhost:8080`.

## Ejecutar frontend

Desde NetBeans:
1. Abrir `frontend` como proyecto Maven Web Application.
2. Registrar/configurar Apache Tomcat.
3. Usar el puerto `8081`.
4. Ejecutar el proyecto.
5. Abrir `http://localhost:8081/monitoreo/`.

## Usuario de prueba

El inicializador crea:

- Correo: `admin@monitoreo.com`
- Contraseña: `Admin123`

También se puede registrar un usuario desde la pantalla de acceso.

## Funcionalidades

### Dashboard
- Tarjetas con temperatura, humedad y CO₂.
- Gráficos configurables.
- Crear gráficos.
- Eliminar gráficos.
- Actualización de lecturas en tiempo real mediante WebSocket.

### Catálogo de sensores
- Catálogo independiente de los sensores físicos actualmente creados.
- Alta de nuevos tipos de sensor con unidad y cantidad disponible.
- Activación/desactivación y ajuste de cantidad.
- El catálogo no desaparece cuando se elimina un sensor.

### Sensores
- Lista de sensores.
- Estado.
- Última lectura.
- Valor actual.
- CRUD básico de sensores desde API REST.

### Reportes
- Consulta de lecturas.
- Filtro por texto.
- Filtro por sensor.
- Exportación de los resultados a un archivo XLSX compatible con Microsoft Excel.

## Endpoints principales

### Autenticación

`POST /api/auth/register`

```json
{
  "nombre": "Daniel",
  "correo": "daniel@example.com",
  "password": "123456"
}
```

`POST /api/auth/login`

```json
{
  "correo": "admin@monitoreo.com",
  "password": "Admin123"
}
```

### Catálogo de sensores
- Catálogo independiente de los sensores físicos actualmente creados.
- Alta de nuevos tipos de sensor con unidad y cantidad disponible.
- Activación/desactivación y ajuste de cantidad.
- El catálogo no desaparece cuando se elimina un sensor.

### Sensores

- `GET /api/sensores`
- `GET /api/sensores/{id}`
- `POST /api/sensores`
- `PUT /api/sensores/{id}`
- `PATCH /api/sensores/{id}/estado`
- `DELETE /api/sensores/{id}`

### Catálogo de tipos de sensores
- `GET /api/tipos-sensores`
- `GET /api/tipos-sensores?activos=false`
- `POST /api/tipos-sensores`
- `PUT /api/tipos-sensores/{id}`
- `PATCH /api/tipos-sensores/{id}/activo`
- `DELETE /api/tipos-sensores/{id}`

### Gráficos

- `GET /api/graficos`
- `GET /api/graficos/{id}`
- `POST /api/graficos`
- `PUT /api/graficos/{id}`
- `PATCH /api/graficos/{id}/activo`
- `DELETE /api/graficos/{id}`

### Lecturas

- `GET /api/lecturas`
- `GET /api/lecturas/{id}`
- `GET /api/lecturas/sensor/{sensorId}`
- `POST /api/lecturas`
- `DELETE /api/lecturas/{id}`

### Reportes

`GET /api/reportes`

Parámetros opcionales:

- `sensorId`
- `desde` en formato `yyyy-MM-dd`
- `hasta` en formato `yyyy-MM-dd`

## WebSocket

El frontend se conecta a:

`ws://localhost:8080/ws/sensores`

Cada actualización tiene una estructura similar a:

```json
{
  "tipo": "lectura",
  "sensorId": 1,
  "sensor": "Temperatura",
  "valor": 26.4,
  "unidad": "°C",
  "fecha": "2026-07-28T13:00:00"
}
```

## Git

Ejemplo de commits:

```bash
git add .
git commit -m "Agregar API REST de sensores y lecturas"
git commit -m "Implementar CRUD de graficos"
git commit -m "Agregar dashboard web y actualizacion por WebSocket"
git commit -m "Agregar reportes y documentacion de endpoints"
git push
```

## Nota de seguridad

Las credenciales de MySQL son locales. No subir contraseñas reales al repositorio público.
