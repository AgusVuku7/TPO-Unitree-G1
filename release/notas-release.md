## Título del release
Vigía v1.0 · Entrega

## Tag
v1.0 (sobre `main`)

## Descripción (para pegar en GitHub)

Primera versión instalable de **Vigía**, la consola HMI para operar un robot Unitree G1 simulado (escolta de visitantes en un centro logístico).

### Archivos
- `vigia-v1.0-debug.apk`: APK instalable (debug), Android 8.0 o superior. El diseño principal es para tablet en horizontal.
- `vigia-presentacion.mp4`: video de demostración (2:49) con los cinco escenarios.

### Instalación
1. Descargar el APK en el dispositivo (o arrastrarlo a un emulador).
2. Permitir la instalación de apps de origen desconocido si lo pide.
3. Abrir Vigía → **Conectar** en G1 · GO-01.

No necesita robot, servidor ni internet: incluye su propio simulador.

### Qué incluye
- Supervisión de la escolta: recorrido, telemetría y alertas.
- Órdenes: iniciar, pausar, reanudar, cancelar, ir a punto seguro, detener y reconocer alertas, cada una con su estado.
- Cinco escenarios de prueba: escolta normal, batería baja, desconexión, incidente (inclinación) y visitante rezagado.
- Historial guardado en el dispositivo (Room).

### Limitaciones
- *Detener* es un comando de software simulado: no reemplaza una parada de emergencia física.
- En teléfono la app funciona, pero el diseño no replica pantalla por pantalla el de Figma.

SHA-256 del APK: `b13c47b7b0c737bada6bca69fb4cc0d1e1559d63da7c5ff791ac5ce7ca374775`
