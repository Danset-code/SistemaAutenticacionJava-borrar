# Vinculación automática de microcontroladores y aislamiento por usuario

## Objetivo

El sistema asigna automáticamente nombres legibles como `device1`, `device2`, etc. El usuario no escribe el nombre del microcontrolador ni debe inventar una etiqueta como `ESP32-001`. El firmware envía el identificador técnico estable del equipo y el backend lo asocia a la cuenta que generó el código de vinculación.

Cada dispositivo y sus sensores quedan vinculados a una cuenta. Las lecturas, los gráficos, el historial, los reportes y los mensajes WebSocket se filtran por el propietario de esa cuenta. El catálogo de tipos de sensor es común porque contiene definiciones de variables y unidades, no lecturas de un cultivo.

## Vincular un dispositivo por primera vez

1. Inicia sesión en el Sistema de Monitoreo.
2. Abre **Dispositivos** y selecciona **Vincular dispositivo**.
3. Copia el código de vinculación de un solo uso que aparece para el nuevo alias (por ejemplo, `device1`).
4. En la configuración inicial del firmware, define la URL del backend y el código recibido. El firmware debe calcular o leer el identificador estable del hardware automáticamente.
5. El equipo envía `hardwareId` y `pairingCode` a `POST /api/dispositivos/vincular`. La respuesta contiene el alias asignado y un `deviceKey` privado.
6. El firmware guarda el `deviceKey` de forma persistente y no lo imprime en logs públicos ni lo incluye en la interfaz del usuario. La clave debe permanecer en el dispositivo.

El código de vinculación no es un nombre para el dispositivo: únicamente sirve para autorizar su primera asociación con una cuenta. Una vez utilizado, deja de ser válido.

### Petición inicial de vinculación

`POST https://TU-BACKEND/api/dispositivos/vincular`

```json
{
  "hardwareId": "IDENTIFICADOR_UNICO_LEIDO_POR_EL_FIRMWARE",
  "pairingCode": "CODIGO_DE_UN_SOLO_USO"
}
```

Respuesta orientativa:

```json
{
  "success": true,
  "alias": "device1",
  "hardwareId": "IDENTIFICADOR_UNICO_LEIDO_POR_EL_FIRMWARE",
  "deviceKey": "CLAVE_PRIVADA_DEVUELTA_POR_EL_BACKEND",
  "message": "Dispositivo vinculado"
}
```

No copies literalmente los valores de ejemplo. El `hardwareId` debe ser estable para la misma placa y distinto entre placas. El `deviceKey` es secreto y debe almacenarse en el propio equipo.

## Cómo obtener un ID estable según el hardware

### ESP32 (Arduino framework)

En muchos ESP32 puede obtenerse un identificador derivado de su eFuse/MAC. Ejemplo orientativo:

```cpp
#include <Arduino.h>

String obtenerHardwareId() {
  uint64_t chipId = ESP.getEfuseMac();
  char id[24];
  snprintf(id, sizeof(id), "ESP32-%04X%08X",
           (uint16_t)(chipId >> 32), (uint32_t)chipId);
  return String(id);
}
```

Comprueba que la función esté disponible para la versión concreta del core ESP32 instalada y no cambies el formato después de vincular el dispositivo: la aplicación utiliza el mismo valor para reconocerlo.

### Raspberry Pi

Cuando el sistema expone un número de serie del equipo, el proceso del dispositivo puede leerlo de `/proc/cpuinfo` (campo `Serial`) o de `/sys/firmware/devicetree/base/serial-number`, según el modelo y el sistema operativo. Si no existe un identificador estable disponible, genera uno una sola vez y guárdalo de forma persistente; no generes un UUID nuevo en cada arranque.

### Arduino u otras placas

No todas las placas Arduino clásicas exponen un número de serie de hardware único. Si el microcontrolador no tiene un UID único disponible, genera un identificador aleatorio en la primera ejecución y guárdalo en EEPROM/flash. No clones esa configuración entre placas, porque ambas enviarían la misma identidad.

## Envío de lecturas

Cada lectura enviada por un dispositivo vinculado debe incluir `X-Device-Key`. El `deviceId` del JSON debe coincidir exactamente con el `hardwareId` que se registró durante la vinculación. El nombre visible `device1` no se envía como identidad física.

`POST https://TU-BACKEND/api/lecturas`

Encabezado:

```text
Content-Type: application/json
X-Device-Key: CLAVE_PRIVADA_GUARDADA_EN_EL_DISPOSITIVO
```

Cuerpo:

```json
{
  "deviceId": "IDENTIFICADOR_UNICO_LEIDO_POR_EL_FIRMWARE",
  "canal": "1",
  "tipo": "Temperatura del aire",
  "unidad": "°C",
  "valor": 26.8,
  "fecha": "2026-10-08T20:30:00"
}
```

Para un segundo sensor conectado a la misma placa cambia `canal` (por ejemplo, `2`). Se conserva el mismo `deviceId`. Así pueden registrarse varios canales del mismo equipo, incluso si tienen el mismo tipo de variable. Si el sensor de ese canal todavía no existe, el backend puede crearlo a partir de `tipo` y `unidad` enviados con la primera lectura.

## Seguridad y compatibilidad

- El navegador utiliza un token de sesión en memoria. Al recargar la URL, debe aparecer de nuevo el inicio de sesión.
- Las API privadas de sensores, dispositivos, gráficos, lecturas y reportes requieren una sesión válida. Los equipos físicos usan su `X-Device-Key` para enviar lecturas.
- El código de vinculación es de un solo uso; no lo publiques ni lo compartas con otros equipos.
- No uses `sensorId` para identificar lecturas desde el microcontrolador. El backend resuelve el sensor usando el `deviceId` registrado y el `canal`.
- Las claves deben transmitirse por HTTPS en producción. En desarrollo local puede utilizarse la URL local del backend.
- La primera migración conserva los sensores históricos sin propietario asignándolos al administrador, para evitar perder la información ya almacenada. Los registros nuevos de cada usuario se guardan bajo su propietario.

## Prueba de aislamiento

1. Inicia sesión como administrador y confirma que ve los sensores históricos.
2. Registra un usuario nuevo. Su dashboard debe empezar vacío.
3. Vincula un dispositivo con ese usuario, crea o envía una lectura y verifica que aparecen sus datos.
4. Cierra sesión y vuelve a entrar como administrador. Los datos recién creados por el usuario de prueba no deben aparecer en el dashboard, los sensores, los reportes ni las actualizaciones WebSocket del administrador.
