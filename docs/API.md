# Documentación API REST - Sistema de Monitoreo

Base URL:

`http://localhost:8080`

## 1. Autenticación

### POST /api/auth/register

Registra un usuario.

Body:

```json
{
  "nombre": "Daniel",
  "correo": "daniel@example.com",
  "password": "123456"
}
```

Respuestas:
- `201 Created`: registro exitoso.
- `400 Bad Request`: datos inválidos o correo existente.

### POST /api/auth/login

Valida las credenciales.

Body:

```json
{
  "correo": "admin@monitoreo.com",
  "password": "Admin123"
}
```

Respuestas:
- `200 OK`: credenciales válidas.
- `401 Unauthorized`: credenciales incorrectas.
- `400 Bad Request`: datos inválidos.

## 2. Sensores

Recurso: `/api/sensores`

| Método | Endpoint | Función |
|---|---|---|
| GET | `/api/sensores` | Lista sensores |
| GET | `/api/sensores/{id}` | Consulta un sensor |
| POST | `/api/sensores` | Crea sensor |
| PUT | `/api/sensores/{id}` | Actualiza sensor |
| PATCH | `/api/sensores/{id}/estado` | Cambia estado |
| DELETE | `/api/sensores/{id}` | Elimina sensor |

POST/PUT:

```json
{
  "nombre": "Temperatura",
  "tipo": "Temperatura",
  "unidad": "°C",
  "estado": "ACTIVO"
}
```

## 3. Gráficos

Recurso: `/api/graficos`

| Método | Endpoint | Función |
|---|---|---|
| GET | `/api/graficos` | Lista gráficos |
| GET | `/api/graficos/{id}` | Consulta gráfico |
| POST | `/api/graficos` | Crea gráfico |
| PUT | `/api/graficos/{id}` | Actualiza gráfico |
| PATCH | `/api/graficos/{id}/activo` | Activa/desactiva |
| DELETE | `/api/graficos/{id}` | Elimina gráfico |

POST:

```json
{
  "nombre": "Temperatura ambiente",
  "tipo": "LINEAL",
  "sensorId": 1,
  "activo": true
}
```

Tipos admitidos por el frontend:
- LINEAL
- BARRAS
- AREA

## 4. Lecturas

Recurso: `/api/lecturas`

| Método | Endpoint | Función |
|---|---|---|
| GET | `/api/lecturas` | Lista lecturas |
| GET | `/api/lecturas/{id}` | Consulta lectura |
| GET | `/api/lecturas/sensor/{sensorId}` | Lecturas de un sensor |
| POST | `/api/lecturas` | Registra lectura |
| DELETE | `/api/lecturas/{id}` | Elimina lectura |

POST:

```json
{
  "sensorId": 1,
  "valor": 25.8,
  "fecha": "2026-07-28T13:30:00"
}
```

## 5. Reportes

### GET /api/reportes

Consulta lecturas para reportes.

Ejemplos:

`GET /api/reportes`

`GET /api/reportes?sensorId=1`

`GET /api/reportes?desde=2026-07-01&hasta=2026-07-28`

## 6. WebSocket

Endpoint:

`ws://localhost:8080/ws/sensores`

El servidor transmite una lectura nueva cada 5 segundos para cada sensor ficticio.

El mensaje contiene:

```json
{
  "tipo": "lectura",
  "sensorId": 1,
  "sensor": "Temperatura",
  "valor": 26.3,
  "unidad": "°C",
  "fecha": "2026-07-28T13:30:00"
}
```

## Códigos HTTP

- `200 OK`: consulta/actualización exitosa.
- `201 Created`: recurso creado.
- `204 No Content`: eliminación exitosa.
- `400 Bad Request`: datos inválidos.
- `401 Unauthorized`: credenciales incorrectas.
- `404 Not Found`: recurso no encontrado.
- `409 Conflict`: conflicto, por ejemplo correo duplicado.
- `500 Internal Server Error`: error no controlado.

## 6. Catálogo de tipos de sensores

Recurso: `/api/tipos-sensores`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/tipos-sensores` | Lista tipos activos |
| GET | `/api/tipos-sensores?activos=false` | Lista todos los tipos |
| GET | `/api/tipos-sensores/{id}` | Consulta un tipo |
| POST | `/api/tipos-sensores` | Agrega un tipo al catálogo |
| PUT | `/api/tipos-sensores/{id}` | Actualiza nombre, unidad y cantidad disponible |
| PATCH | `/api/tipos-sensores/{id}/activo?activo=true|false` | Activa/desactiva un tipo |
| DELETE | `/api/tipos-sensores/{id}` | Elimina un tipo del catálogo |

El catálogo es independiente de los sensores físicos creados. Por eso eliminar un sensor o un gráfico no elimina su tipo del catálogo.

## 7. Exportación Excel

`GET /api/reportes/exportar` genera un archivo **XLSX real** utilizando Apache POI. Acepta los mismos filtros opcionales del reporte (`sensorId`, `desde`, `hasta`) y responde con `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` y `Content-Disposition: attachment`.

## Vinculación física de sensores

Las lecturas pueden enviarse utilizando `deviceId` y `canal` en lugar de `sensorId`:

```json
{
  "deviceId": "ESP32-001",
  "canal": "1",
  "tipo": "CO2",
  "unidad": "ppm",
  "valor": 450.0,
  "fecha": "2026-09-10T18:30:00"
}
```

La combinación `deviceId + canal` identifica de forma única un sensor físico dentro del sistema y permite tener varios sensores del mismo tipo en el mismo microcontrolador.
