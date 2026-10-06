# Vinculación automática de sensores físicos

## Identidad del sensor

Cada sensor físico se identifica por la combinación:

- `deviceId`: identificador del microcontrolador, por ejemplo `ESP32-001`.
- `canal`: identificador del sensor/canal dentro de ese microcontrolador, por ejemplo `1`, `2`, `3`.

Esto permite tener varios sensores del mismo tipo en un mismo microcontrolador. Por ejemplo:

| deviceId | canal | tipo |
|---|---:|---|
| ESP32-001 | 1 | Temperatura |
| ESP32-001 | 2 | Temperatura |
| ESP32-001 | 3 | Humedad |
| ESP32-001 | 4 | CO2 |

El microcontrolador no necesita conocer el `id` autogenerado por MySQL ni el `id` del gráfico.

## Lectura recomendada desde el microcontrolador

```json
{
  "deviceId": "ESP32-001",
  "canal": "1",
  "tipo": "Temperatura del aire",
  "unidad": "°C",
  "valor": 26.7,
  "fecha": "2026-09-10T18:30:00"
}
```

Endpoint:

`POST http://localhost:8080/api/lecturas`

## Resolución automática en el backend

1. Busca `deviceId + canal`.
2. Si existe, utiliza el sensor configurado.
3. Si no existe, crea el sensor utilizando `tipo` y `unidad` recibidos.
4. Busca un gráfico asociado al sensor.
5. Si no existe gráfico, crea uno LINEAL automáticamente.
6. Guarda la lectura.
7. Actualiza `valorActual`, `ultimaLectura` y `estado=ACTIVO`.
8. Publica el evento mediante WebSocket `/ws/sensores`.

## Estado de comunicación

Un sensor nuevo queda `INACTIVO` hasta que recibe una lectura. Cada lectura lo pasa a `ACTIVO`. Si pasan más de 10 segundos sin una nueva lectura, el backend lo cambia automáticamente a `INACTIVO` y envía el cambio por WebSocket.

## Frontend

El dashboard recibe el evento WebSocket y actualiza inmediatamente el sensor y el gráfico asociado. No es necesario recargar la página.
