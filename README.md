# Sistema de Monitoreo de Cultivo Medicinal

Proyecto formativo para visualizar y administrar datos ambientales de un cultivo medicinal mediante una aplicación web, API REST, MySQL y WebSocket.

## Arquitectura

- **Backend:** Java 17, Spring Boot 3.5.0, Spring Data JPA, MySQL y WebSocket.
- **Frontend:** HTML5, CSS y JavaScript, empaquetado como WAR para Apache Tomcat desde NetBeans.
- **Autenticación:** sesión de servidor identificada mediante `Authorization: Bearer <accessToken>`.
- **Datos por cuenta:** sensores, gráficos, lecturas, reportes y eventos WebSocket quedan filtrados por el usuario autenticado.
- **Tipos de sensor:** el listado de variables y unidades es un catálogo común. Los sensores físicos y sus lecturas no son compartidos.

## Puertos de desarrollo

- Backend Spring Boot: `http://localhost:8080`
- Frontend Tomcat: `http://localhost:8081/monitoreo/`
- API: `http://localhost:8080/api`
- WebSocket: `ws://localhost:8080/ws/sensores?token=<accessToken>`

Configura Tomcat en el puerto HTTP `8081` para evitar conflictos con Spring Boot.

## Base de datos

Crear en MySQL:

```sql
CREATE DATABASE monitoreo_cultivo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Configura las variables de entorno `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` según tu entorno. El archivo `backend/src/main/resources/application.properties` conserva la configuración local del proyecto; antes de publicar el sistema, no dejes credenciales de la base de datos escritas en el repositorio.

Hibernate utiliza `ddl-auto=update` para añadir las tablas y columnas del modelo actualizado. **Antes de iniciar por primera vez con una base existente, haz una copia de seguridad.** El inicializador asigna los sensores históricos que no tengan propietario al administrador para no perder sus datos; los usuarios nuevos empiezan sin sensores, gráficos ni lecturas.

## Ejecutar en NetBeans

### Backend

1. Abrir la carpeta `backend` como proyecto Maven.
2. Verificar que esté seleccionada una JDK compatible con Java 17.
3. Ejecutar `Main.java` o `spring-boot:run`.
4. Revisar la consola y confirmar que Spring Boot termina de iniciar correctamente.

### Frontend

1. Abrir `frontend` como proyecto Maven Web Application.
2. Registrar/configurar Apache Tomcat con puerto HTTP `8081`.
3. Ejecutar el proyecto.
4. Abrir `http://localhost:8081/monitoreo/`.
5. Cada nueva carga de la URL comienza en el inicio de sesión; el token se conserva únicamente en memoria del navegador.

## Cuenta administradora de desarrollo

El inicializador crea la cuenta de desarrollo si todavía no existe:

- Correo: `admin@monitoreo.com`
- Contraseña inicial: `Admin123`

Cambia las credenciales de desarrollo antes de utilizarlo fuera del entorno local. El proyecto también permite registrar usuarios desde la pantalla de acceso.

## Aislamiento por usuario

Los endpoints privados requieren el encabezado `Authorization: Bearer <accessToken>`. El token se obtiene de `POST /api/auth/login`; la respuesta contiene `accessToken`. Al cerrar sesión se invalida el token en el backend. Si recargas la URL, debes iniciar sesión de nuevo.

Los endpoints de sensores, gráficos, lecturas, reportes y dispositivos solo devuelven o modifican los datos del propietario autenticado. Los mensajes WebSocket se envían únicamente a sesiones de la cuenta dueña del sensor.

## Dispositivos y canales

El backend asigna automáticamente alias legibles `device1`, `device2`, etc. El firmware no debe enviar ese alias como identidad física: debe enviar un identificador estable y único (`deviceId`). El alias es una etiqueta que genera la aplicación y muestra al usuario.

Para vincular un dispositivo por primera vez, inicia sesión, abre **Dispositivos** y genera un código de vinculación. El firmware intercambia una sola vez ese código y el ID real del hardware por una clave privada `deviceKey`. En los envíos posteriores debe utilizar `X-Device-Key` y mandar `deviceId`, `canal`, `tipo`, `unidad`, `valor` y opcionalmente `fecha` en el cuerpo JSON.

Guía completa, ejemplos de petición y recomendaciones por hardware: [`docs/DEVICE_LINKING.md`](docs/DEVICE_LINKING.md).

Ejemplo de lectura del dispositivo ya vinculado:

```json
{
  "deviceId": "IDENTIFICADOR_UNICO_DE_HARDWARE",
  "canal": "1",
  "tipo": "Temperatura del aire",
  "unidad": "°C",
  "valor": 26.8,
  "fecha": "2026-10-08T20:30:00"
}
```

Endpoint local: `POST http://localhost:8080/api/lecturas`.

Encabezado: `X-Device-Key: <deviceKey>`.

Un equipo puede reportar varios canales usando el mismo `deviceId` y distinto `canal`. Si el canal aún no tiene sensor, la primera lectura debe incluir `tipo` y `unidad` para permitir su creación automática. En producción utiliza HTTPS para proteger la clave del dispositivo.

## Endpoints principales

### Autenticación

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/usuarios/{id}`
- `PUT /api/auth/usuarios/{id}`
- `DELETE /api/auth/usuarios/{id}`

### Sensores, gráficos, lecturas y reportes

- Sensores: `GET/POST /api/sensores`, `GET/PUT/DELETE /api/sensores/{id}`
- Gráficos: `GET/POST /api/graficos`, `GET/PUT/DELETE /api/graficos/{id}`
- Lecturas: `GET/POST /api/lecturas`, `GET/DELETE /api/lecturas/{id}`
- Reportes: `GET /api/reportes`, `GET /api/reportes/exportar`
- Dispositivos: `GET /api/dispositivos`, `POST /api/dispositivos/codigo-vinculacion`, `POST /api/dispositivos/vincular`
- Catálogo compartido de tipos de sensores: `GET /api/tipos-sensores`

Las operaciones privadas desde Postman también deben incluir `Authorization: Bearer <accessToken>`; para probarlo, primero inicia sesión con `POST /api/auth/login` y copia el `accessToken` devuelto.

## Prueba básica de aislamiento

1. Inicia sesión con el administrador y verifica los registros históricos.
2. Registra una cuenta nueva e inicia sesión con ella; el dashboard debe comenzar vacío.
3. Crea un dispositivo, sensor, gráfico y lectura con la segunda cuenta.
4. Cierra sesión y entra como administrador. Los datos creados por la segunda cuenta no deben aparecer en sensores, dashboard, reportes ni mensajes WebSocket.
5. Recarga la URL en una sesión activa. Debe solicitar de nuevo las credenciales.
