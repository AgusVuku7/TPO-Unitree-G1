# TPO: Interfaz HMI/HRI para Robot de Servicio Unitree G1 — "Vigía"

Trabajo Práctico Grupal para la materia **Desarrollo de Aplicaciones** (UADE - Facultad de Ingeniería y Ciencias Exactas).

---

## 🤖 Descripción del Proyecto

**Vigía** es una interfaz HMI (Human-Machine Interface) y HRI (Human-Robot Interaction) en formato aplicación Android para operar y supervisar misiones de un robot humanoide **Unitree G1** en entornos industriales y logísticos.

El robot realiza tareas de **escolta y control de acceso** para visitantes en un recorrido definido:
$$\text{Base} \rightarrow \text{P1 (Hall)} \rightarrow \text{P2 (Pasillo)} \rightarrow \text{P3 (Cruce de Autoelevadores)} \rightarrow \text{P4 (Oficina)}$$

Avanza a velocidad de paso humano ($0{,}5\text{ m/s}$), deteniéndose por seguridad en el cruce de autoelevadores hasta recibir confirmación explícita del operador humano.

---

## 📱 Arquitectura de la Aplicación (`VigiaApp`)

La aplicación móvil está construida con las mejores prácticas modernas de Android:
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Patrón:** MVVM (Model-View-ViewModel) con `StateFlow`
- **Concurrencia:** Kotlin Coroutines
- **Persistencia Local:** Room Database (100% offline, almacena misiones y eventos)
- **Navegación:** Navigation Compose con barra inferior persistente (Panel, Alertas e Historial)

---

## 📂 Estructura del Repositorio

```text
├── VigiaApp/                     # Proyecto Android nativo (Jetpack Compose)
│   ├── app/
│   │   ├── src/main/java/com/uade/vigia/
│   │   │   ├── data/            # Modelos y SimulationRepository (IRobotRepository)
│   │   │   │   └── local/       # Room Database (Entities, Dao, VigiaDatabase)
│   │   │   ├── ui/              # VigiaViewModel, UiState
│   │   │   │   ├── components/  # DashboardScreen, MapView, ConnectionScreen, HistoryScreen, AlertsScreen
│   │   │   │   └── theme/       # Color, Theme
│   │   │   └── MainActivity.kt  # Punto de entrada y NavHost
│   │   └── build.gradle.kts
│   ├── gradle/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
└── README.md
```

---

## 🚀 Compilación y Ejecución

1. Abrir la carpeta `VigiaApp` en **Android Studio**.
2. Sincronizar Gradle.
3. Ejecutar en un emulador Android (API 26+) o dispositivo físico con el botón **Run ▶️**.

Alternativamente, compilar por línea de comandos desde `VigiaApp/`:
```bash
./gradlew assembleDebug
```
El APK resultante se genera en `VigiaApp/app/build/outputs/apk/debug/app-debug.apk`.
