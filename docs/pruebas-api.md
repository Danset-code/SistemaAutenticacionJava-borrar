# Pruebas de la API

## Registro
POST http://localhost:8080/api/auth/register

```json
{
  "nombre": "Daniel",
  "correo": "daniel@correo.com",
  "password": "123456"
}
```

Esperado: 201 Created.

## Correo duplicado

Esperado: 409 Conflict.

## Login correcto
POST http://localhost:8080/api/auth/login

```json
{
  "correo": "daniel@correo.com",
  "password": "123456"
}
```

Esperado: 200 OK.

## Login incorrecto

Esperado: 401 Unauthorized.

## Datos inválidos

Esperado: 400 Bad Request.
