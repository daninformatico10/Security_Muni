# SegurApp — Sistema Comunal de Gestión de Seguridad
**Municipalidad de San Joaquín**
*Proyecto Académico Semestral — Desarrollo de Aplicaciones Móviles Android*

---

## 📌 Descripción del Proyecto

**SegurApp** es una solución tecnológica nativa desarrollada para la **Municipalidad de San Joaquín**, cuyo objetivo principal es modernizar y agilizar la gestión, recepción y seguimiento de situaciones de seguridad e incidentes dentro de la comuna.

La aplicación permite a los usuarios e inspectores comunales:
1. **Autenticarse de forma segura** en el sistema comunal.
2. **Visualizar un dashboard interactivo** con los incidentes de seguridad reportados recientemente (robos, mascotas perdidas, ruidos molestos, accidentes de tránsito, etc.).
3. **Registrar nuevos casos de seguridad** mediante un formulario dinámico que soporta la selección de categoría, descripción detallada y la captura de evidencia fotográfica y coordenadas GPS.
4. **Visualizar geográficamente los casos en un mapa interactivo (Google Maps)**, permitiendo ubicar georreferenciadamente los reportes comunitarios y capturar coordenadas mediante la interacción directa con el mapa (*onMapLongClick*).

---

## 🛠️ Arquitectura y Tecnologías

El desarrollo de **SegurApp** se construyó bajo los estándares modernos de desarrollo Android recomendados por Google:

* **Lenguaje de Programación:** Kotlin (v2.0+)
* **UI Toolkit:** Jetpack Compose (Material Design 3 - Declarativo)
* **Patrón Arquitectónico:** MVVM (*Model - View - ViewModel*) con separación estricta de responsabilidades.
* **Manejo de Estado:** `StateFlow` y `MutableStateFlow` (Programación reactiva con Kotlin Coroutines y Flow).
* **Servicios de Mapas:** Google Maps Platform (`maps-compose`, `play-services-maps`, `play-services-location`).

### 📐 Justificación de la Arquitectura MVVM
Se eligió la arquitectura **MVVM (Model - View - ViewModel)** para garantizar:
* **Desacoplamiento Estricto:** La capa de presentación (`UI`) desconoce las reglas de negocio internas; únicamente observa estados expuestos por el `ViewModel`.
* **Programación Reactiva y Unidireccional (UDF - *Unidirectional Data Flow*):** El estado fluye en una sola dirección desde el ViewModel hacia la vista Compose, y los eventos de usuario (*clicks*, entrada de texto) fluyen desde la vista hacia los métodos del ViewModel.
* **Mantenibilidad y Escalabilidad:** Facilita la adición de nuevas funcionalidades (como persistencia en base de datos local Room o integración con servicios backend REST API) sin alterar la interfaz visual.
* **Testabilidad:** La lógica de validación y gestión de datos se puede probar mediante pruebas unitarias sin depender del ciclo de vida de la vista.

---

## 📂 Estructura de Carpetas del Proyecto

El código fuente sigue una organización por paquetes limpia basada en la responsabilidad de cada capa dentro del patrón MVVM:

```text
com.example.myapplication/
├── model/
│   └── CasoSeguridad.kt         # Definición de entidades de datos y enums de tipificación
├── viewmodel/
│   ├── LoginViewModel.kt        # Gestión de estado de autenticación y validación de formulario
│   └── CasosViewModel.kt        # Estado global de la lista de casos y formulario de nuevos incidentes
├── ui/
│   ├── LoginScreen.kt           # Vista declarativa del panel de inicio de sesión
│   ├── CasosScreen.kt           # Dashboard de casos, formulario de registro y navegación por pestañas
│   ├── MapaCasosScreen.kt       # Componente del mapa interactivo con Google Maps
│   └── theme/                   # Sistema de diseño (Colores, Tipografía, Tema Material 3)
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt              # ComponentActivity principal y enrutador del flujo de la app
```

### 🔍 Responsabilidad de cada Carpeta

* **`model/` (Capa de Modelo):**
  Contiene las estructuras de datos puras de Kotlin (`data class` e `enum class`). Define la entidad `CasoSeguridad` y el tipo enumerado `CategoriaCaso` (`ROBO`, `PERRO_PERDIDO`, `RUIDOS_MOLESTOS`, `ACCIDENTE`, `OTRO`). No contiene referencias a Compose ni a contextos de Android.

* **`viewmodel/` (Capa de ViewModel):**
  Responsable de almacenar y administrar el estado de la UI consciente del ciclo de vida. Mantiene las variables `StateFlow` inmutables expuestas a la vista (`LoginUiState`, `FormularioCasoUiState`, lista de `CasoSeguridad`). Contiene las validaciones de campos vacíos, credenciales de inicio de sesión y la adición reactiva de nuevos elementos.

* **`ui/` (Capa de Vista - Presentation):**
  Contiene las funciones `@Composable` escritas de manera declarativa. Se divide en componentes *stateful* (conectados con el ViewModel) y *stateless* (utilizados para pruebas y previews).

* **`ui/theme/` (Sistema de Diseño):**
  Centraliza la paleta de colores institucionales de San Joaquín, tipografías y el tema visual unificado en Material Design 3.

---

## 🧠 Decisiones de Código y Lógica Clave

1. **Uso de `StateFlow` y `asStateFlow()`:**
   Para asegurar la reactividad y evitar modificaciones no autorizadas del estado desde la vista, los `ViewModels` mantienen una propiedad privada mutable (`_uiState`) y exponen una versión inmutable observable (`uiState = _uiState.asStateFlow()`).

2. **Validaciones Centralizadas e Iconografía Informativa:**
   En el formulario de inicio de sesión y registro de casos, las validaciones de campos obligatorios se procesan en el `ViewModel`. Cuando un campo falla, el estado emite un mensaje de error y activa la bandera `isError = true` en los componentes `OutlinedTextField`, mostrando un ícono descriptivo (`Icons.Default.Warning`) y un texto de alerta de color rojo bajo el campo afectado.

3. **Navegación por Pestañas (`TabRow`):**
   La pantalla [CasosScreen](file:///C:/Users/felipe/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/CasosScreen.kt) implementa un `TabRow` de Material 3 con 3 pestañas:
   * **Pestaña 1 - Casos:** Muestra el `DashboardCasos` con un `LazyColumn` eficiente.
   * **Pestaña 2 - Registrar:** Muestra el `FormularioRegistroCaso` para crear nuevos reportes.
   * **Pestaña 3 - Mapa:** Carga la vista del mapa comunal `MapaCasos`.

4. **Integración con Google Maps SDK (`maps-compose`):**
   El componente `MapaCasos` inicializa la cámara centrada geográficamente en la comuna de San Joaquín (`Latitud: -33.4852, Longitud: -70.6258`). Renderiza dinámicamente marcadores (`Marker`) para cada caso registrado con coordenadas e implementa el evento `onMapLongClick` que permite seleccionar un punto en el mapa y capturar sus coordenadas.

---

## 🚀 Estado Actual de Desarrollo

Hasta la fecha, se ha completado con éxito la primera fase del sistema:

| Módulo / Funcionalidad | Estado | Descripción |
| :--- | :---: | :--- |
| **Inicio de Sesión (Login)** | 🟢 Completado | Autenticación con credenciales predeterminadas (`admin` / `123`), validaciones por campo y mensajes de error. |
| **Dashboard de Casos** | 🟢 Completado | Lista optimizada con `LazyColumn` y tarjetas diferenciadas por color e ícono según la categoría. |
| **Formulario de Registro** | 🟢 Completado | Campos de entrada, selector de categoría en chips, y botones estilizados para simulación de captura de cámara y ubicación GPS. |
| **Mapa Interactivo Comunal** | 🟢 Completado | Integración de Google Maps Platform con marcadores por incidente y captura de coordenadas mediante `onMapLongClick`. |
| **Navegación por Pestañas** | 🟢 Completado | Menú superior con `TabRow` para alternar fluidamente entre Lista, Formulario y Mapa. |
| **Vistas Previas (@Preview)** | 🟢 Completado | Implementadas en todos los componentes para renderización estática en Android Studio. |

---

*Proyecto desarrollado para la asignatura de Desarrollo de Aplicaciones Móviles — Municipalidad de San Joaquín.*
