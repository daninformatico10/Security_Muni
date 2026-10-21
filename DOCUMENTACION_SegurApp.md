# SegurApp — Documentación del código

App Android para la gestión de casos de seguridad comunal (San Joaquín). MVP académico (DSY1105, Duoc UC) que funciona **100 % offline**, con base de datos local y datos ficticios.

---

## 1. Resumen técnico

| Aspecto | Detalle |
|---|---|
| Lenguaje | Kotlin (1.9.24) |
| UI | Jetpack Compose + Material 3 (Compose BOM 2024.06.00) |
| Navegación | `navigation-compose` 2.7.7 |
| Persistencia | Room 2.6.1 sobre SQLite (procesador KSP 1.9.24-1.0.20) |
| Arquitectura | MVVM simplificada: `Pantalla (Compose)` → `ViewModel` → `Repositorio` → `DAO / Room` |
| Paquete / appId | `cl.duoc.segurapp` |
| SDK | `minSdk 26`, `targetSdk 34`, `compileSdk 34` |
| Java / JVM | 17 |
| Android Gradle Plugin | 8.5.2 |

### Ejecución
1. Abrir la carpeta `android/` en Android Studio (Koala o superior, JDK 17).
2. Esperar el Gradle Sync (requiere internet la primera vez).
3. Ejecutar en emulador o dispositivo.

### Usuarios de prueba (contraseña `demo1234`)

| Perfil | Correo |
|---|---|
| Operador | operador@demo.cl |
| Residente | residente@demo.cl |
| Supervisor | supervisor@demo.cl |
| Administrador | administrador@demo.cl |

---

## 2. Estructura del proyecto

```
SegurApp_local/
├── README.md
└── android/
    ├── build.gradle.kts            Plugins (AGP, Kotlin, KSP) a nivel proyecto
    ├── settings.gradle.kts         Repositorios y módulo :app
    ├── gradle.properties / local.properties / gradle/wrapper
    └── app/
        ├── build.gradle.kts        Configuración del módulo y dependencias
        └── src/main/
            ├── AndroidManifest.xml
            ├── res/values/themes.xml
            └── java/cl/duoc/segurapp/
                ├── MainActivity.kt
                ├── data/
                │   ├── Models.kt         Modelos de dominio y catálogos fijos
                │   ├── Repositorio.kt    Lógica de negocio, sesión y cifrado
                │   └── local/
                │       ├── AppDatabase.kt
                │       ├── Entidades.kt
                │       └── Daos.kt
                └── ui/
                    ├── AppNav.kt         Grafo de navegación raíz
                    ├── MainScreen.kt     Contenedor con barra inferior
                    ├── Components.kt     Componentes reutilizables
                    ├── theme/Theme.kt    Colores y tema
                    ├── auth/             Login, registro y AuthViewModel
                    └── casos/            Lista, detalle, nuevo caso, perfil y CasosViewModel
```

---

## 3. Flujo general de la aplicación

```
MainActivity
  └─ SegurAppTheme
       └─ AppNav (NavHost)
            ├─ "login"        → LoginScreen
            ├─ "registro"     → RegisterScreen
            ├─ "main"         → MainScreen (pestañas: Casos | Nuevo | Perfil)
            └─ "detalle/{id}" → DetalleScreen
```

1. La app inicia en `login`.
2. Al autenticarse (o registrarse) navega a `main` y elimina `login` de la pila (`popUpTo("login") { inclusive = true }`).
3. En `main`, una `NavigationBar` alterna entre **Casos**, **Nuevo** y **Perfil**.
4. Tocar un caso navega a `detalle/{id}` (argumento `Int`).
5. Cerrar sesión limpia el estado (`CasosViewModel.limpiar()`, `AuthViewModel.cerrarSesion()`) y vuelve a `login`.

Los dos ViewModels (`AuthViewModel`, `CasosViewModel`) se crean **una sola vez en `AppNav`** y se pasan a las pantallas, por lo que comparten estado entre destinos.

---

## 4. Capa de datos (`data/`)

### 4.1 `Models.kt` — Modelos de dominio

| Clase | Descripción |
|---|---|
| `Opcion(id, nombre)` | Elemento de una lista de selección. |
| `Catalogos(categorias, sectores)` | Agrupa las listas fijas. |
| `Usuario(id, nombre, correo, rol, sector?)` | Usuario autenticado (no incluye hash ni sal). |
| `Evento(texto, creadoEn)` | Entrada del historial de un caso. |
| `Caso(...)` | Caso para la UI. Incluye `codigo` (`CASO-0001`), `titulo` y `historial` opcional (solo en el detalle). |
| `NuevoCasoRequest(categoriaId, sectorId, descripcion, prioridad, evidenciaSimulada)` | Datos del formulario de alta. |

**Constantes globales**

- `ESTADOS`: `Registrado → En revisión → Derivado → Seguimiento → Cerrado`. El **orden de la lista define el flujo** y se usa con `indexOf` para validar transiciones.
- `CATALOGOS`: categorías (Alumbrado público, Ruidos molestos, Sitio eriazo, Seguridad vial, Basura y escombros, Otro) y sectores (Norte, Centro, Sur, Oriente, Poniente). Los `id` se asignan por posición (`i + 1`).

### 4.2 `data/local/Entidades.kt` — Tablas Room

| Entidad (tabla) | Campos | Notas |
|---|---|---|
| `UsuarioEntity` (`usuarios`) | `id`, `nombre`, `correo`, `hash`, `salt`, `rol`, `sector?` | Índice **único** sobre `correo`. |
| `CasoEntity` (`casos`) | `id`, `titulo?`, `categoria`, `sector`, `descripcion`, `prioridad`, `estado`, `responsable`, `evidenciaSimulada`, `creadoPor?`, `creadoEn` | `titulo` nulo → la UI usa la categoría. Fechas como texto `yyyy-MM-dd HH:mm:ss`. |
| `EventoEntity` (`eventos`) | `id`, `casoId`, `texto`, `creadoEn` | FK a `casos.id` con `ON DELETE CASCADE` e índice en `casoId`. |

### 4.3 `data/local/Daos.kt` — Acceso a datos

**`UsuarioDao`**: `insertar`, `porCorreo(correo)`, `contar()`.

**`CasoDao`**: `insertar`, `actualizar`, `todos()`, `deUsuario(usuarioId)`, `porId(id)`, `insertarEvento`, `insertarEventos`, `eventos(casoId)`.
Todas las listas se ordenan por `creadoEn DESC, id DESC` (más recientes primero). Todas las funciones son `suspend`.

### 4.4 `data/local/AppDatabase.kt`

Base `segurapp.db`, versión 1, `exportSchema = false`. Expone `usuarios()` y `casos()`. Implementa **singleton** con doble verificación (`@Volatile` + `synchronized`) en `AppDatabase.get(context)`.

> No define migraciones: si cambia el esquema hay que subir la versión y agregar una migración (o desinstalar la app durante el desarrollo).

### 4.5 `data/Repositorio.kt` — Lógica de negocio

Es el núcleo de la aplicación. Singleton obtenido con `Repositorio.get(context)`.

#### Elementos auxiliares

| Elemento | Función |
|---|---|
| `ErrorApp` | Excepción "esperada" cuyo mensaje se muestra tal cual al usuario. |
| `Session` | `object` con `usuario: Usuario?` en memoria (`@Volatile`). Es la sesión activa; no persiste al cerrar la app. |
| `Claves` (privado) | Cifrado de contraseñas con **PBKDF2WithHmacSHA256**, 10 000 iteraciones, clave de 256 bits y sal aleatoria de 16 bytes (Base64). `crear()` devuelve `(sal, hash)`; `verificar()` compara con `MessageDigest.isEqual` (tiempo constante). |
| `ahora()` | Fecha actual con formato `yyyy-MM-dd HH:mm:ss`. |
| `capitalizar()` | Primera letra en mayúscula. |

#### Mecanismos internos

- **`operacion { }`**: envoltorio común de todas las operaciones públicas. Ejecuta en `Dispatchers.IO`, asegura que los datos de ejemplo estén creados (`prepararDatos()`) y devuelve `Result<T>`:
  - `ErrorApp` → `Result.failure` con su mensaje.
  - Otra excepción → mensaje genérico "Error en la base de datos local".
  - `CancellationException` se relanza (respeta la cancelación de corrutinas).
- **`prepararDatos()`**: protegido por `Mutex`; si no hay usuarios llama a `sembrar()` y marca `inicializado`.
- **`sembrar()`**: crea los 4 usuarios demo y 4 casos de ejemplo (IDs 9–12) con su historial.
- **`usuarioActual()`**: devuelve `Session.usuario` o lanza "Sesión inválida".
- **`soloPersonal(u)`**: lanza error si el rol es `residente`.
- **`buscarCaso(id, u)`**: busca el caso; si el usuario es residente y no es el creador, responde "Caso no encontrado" (no revela que existe).
- **`detalle(id, u)`**: caso + historial mapeado a `Evento`.
- **Mapeos**: `UsuarioEntity.aUsuario()` y `CasoEntity.aCaso()` (genera `codigo` como `CASO-%04d`).

#### API pública

| Función | Descripción y reglas |
|---|---|
| `login(correo, password, perfil)` | Busca por correo (`trim` + minúsculas), verifica contraseña y exige que el **rol coincida con el perfil elegido**. Error genérico si correo o clave fallan. |
| `registrar(nombre, correo, password, sectorId)` | Valida sector y que el correo no exista. Crea siempre rol **`residente`**. |
| `casos()` | Residente: solo sus casos (`deUsuario`). Resto: todos. |
| `caso(id)` | Detalle con historial, respetando permisos. |
| `crearCaso(req)` | Valida categoría, sector y descripción no vacía (máx. 500 caracteres). Estado inicial `Registrado`, responsable `Sin asignar (sim.)`. Inserta caso + evento "Caso registrado" en **una transacción**. |
| `agregarObservacion(id, texto)` | Rechaza texto vacío; guarda `Observación: …` (máx. 300 caracteres). Disponible para todos los roles con acceso al caso. |
| `derivar(id)` | Solo personal. Falla si el caso ya está en `Derivado` o posterior. Pasa a `Derivado`, responsable `Unidad de Seguimiento (sim.)`, registra evento. Transaccional. |
| `avanzar(id)` | Solo personal. Pasa al siguiente estado de `ESTADOS`; falla si ya está `Cerrado`. Texto del evento según estado destino ("Revisado por <Rol>", "Derivado a…", "Estado cambiado a …"). Si el destino es `Derivado` actualiza el responsable. Transaccional. |

---

## 5. Capa de interfaz (`ui/`)

### 5.1 `MainActivity.kt`
Activity única. Activa `enableEdgeToEdge()` y monta `SegurAppTheme { AppNav() }`.

### 5.2 `AppNav.kt`
Define el `NavHost` y las 4 rutas. Crea los ViewModels compartidos. La lambda `irAMain` navega a `main` limpiando `login`. En `detalle/{id}` el argumento se declara como `NavType.IntType`.

### 5.3 `MainScreen.kt`
`Scaffold` con barra inferior (`Casos`, `Nuevo`, `Perfil`). La pestaña activa se guarda con `rememberSaveable`. Tras guardar un caso nuevo, vuelve a la pestaña **Casos** (`onGuardado = { tab = 0 }`).

### 5.4 `theme/Theme.kt`
Paleta propia (`Navy`, `Fondo`, `Texto`, `TextoSec`, `Borde`, `HeaderSub`, `Ambar`, `Gris`, `ChipFondo`) y `SegurAppTheme` con `lightColorScheme` (solo tema claro).

### 5.5 `Components.kt` — Componentes reutilizables

| Componente | Uso |
|---|---|
| `AppHeader(titulo, subtitulo?, onBack?)` | Encabezado azul; muestra flecha de retroceso si hay `onBack`. |
| `EtiquetaCampo(texto)` | Rótulo sobre un campo. |
| `CampoTexto(...)` | Campo de texto con soporte de contraseña, teclado, placeholder y líneas mínimas. |
| `Selector(titulo, opciones, seleccionado, onSeleccion)` | Lista desplegable. |
| `BotonPrimario` / `BotonSecundario` | Botones de acción (el secundario admite icono). |
| `OpcionPastilla(texto, seleccionada, onClick)` | Opción tipo "pill" (perfil, prioridad). |
| `Etiqueta(texto, fondo, color)` | Chip informativo. |
| `FiltroChip(texto, seleccionado, onClick)` | Chip de filtro. |
| `colorPrioridad(p)` | `Alta` rojo, `Media` ámbar, otra verde (fondo, texto). |
| `colorEstado(e)` | Color de texto por estado. |
| `formatoFecha(s)` | `2026-10-14 09:30:00` → `14 oct · 09:30` (locale `es-CL`); si falla devuelve el texto original. |

### 5.6 Autenticación (`ui/auth/`)

**`AuthViewModel`** (`AndroidViewModel`)
- Estado observable: `usuario`, `cargando`, `error`; expone `sectores`.
- `login(correo, password, perfil, onOk)`: valida campos no vacíos, llama al repositorio, guarda `Session.usuario` y ejecuta `onOk`.
- `registrar(...)`: valida en orden — campos completos, sector elegido, formato de correo (`Patterns.EMAIL_ADDRESS`), contraseña ≥ 8 caracteres y coincidencia con la confirmación.
- `cerrarSesion()`: limpia `Session` y estado.

**`LoginScreen`**: selector de perfil (Operador, Residente, Supervisor, Administrador) en cuadrícula 2×2, campos de correo y contraseña, botón *Ingresar* y acceso a *Crear cuenta*. Por comodidad de demo, los campos vienen precargados (`operador@demo.cl` / `demo1234`) y al cambiar de perfil se reescribe el correo si termina en `@demo.cl`.

**`RegisterScreen`**: formulario de nombre, correo, sector, contraseña y confirmación. Crea cuentas con perfil Residente.

### 5.7 Casos (`ui/casos/`)

**`CasosViewModel`**
- Estado: `casos`, `detalle`, `cargando`, `error`; expone `catalogos`.
- `cargarCasos()`, `cargarDetalle(id)`, `crearCaso(req, onOk)` (valida descripción), `agregarObservacion`, `derivar`, `avanzar`.
- `accion(...)` (privada) unifica las acciones que devuelven un `Caso` actualizado y lo asignan a `detalle`.
- `limpiar()` reinicia el estado al cerrar sesión.

**`CasosScreen`**: carga la lista al entrar (`LaunchedEffect`), búsqueda por código, categoría o título, y filtros por estado (`Todos`, `En revisión`, `Derivado`, `Cerrado`). Muestra cada caso en una `CasoCard` (código, fecha, título, sector, prioridad y estado). La búsqueda y el filtro se conservan con `rememberSaveable`.

**`DetalleScreen`**: muestra
- Barra de progreso por estados (`Progreso`): completados en azul, actual en ámbar con borde, pendientes en gris.
- Datos del caso (prioridad, sector, responsable, descripción) e historial.
- Campo para **agregar observación** (se limpia al enviar con éxito).
- Para personal (rol ≠ residente): botones **Derivar** (habilitado solo antes de `Derivado`) y **Cambiar estado** (deshabilitado en el último estado).

**`NuevoCasoScreen`**: formulario con categoría, sector, descripción, prioridad (Baja/Media/Alta; por defecto Media), fecha/hora simulada (solo informativa; la fecha real se asigna en el repositorio) y alternador de evidencia simulada. Al guardar con éxito limpia el formulario y vuelve a la lista.

**`PerfilScreen`**: nombre, correo, perfil y sector del usuario, más botón *Cerrar sesión*.

---

## 6. Reglas de negocio

### Permisos por perfil

| Acción | Residente | Operador / Supervisor / Administrador |
|---|:---:|:---:|
| Ver casos | Solo los propios | Todos |
| Crear caso | Sí | Sí |
| Agregar observación | Sí (en sus casos) | Sí |
| Derivar | No | Sí |
| Cambiar estado | No | Sí |
| Registrarse por la app | Sí (rol Residente) | No (solo cuentas demo) |

> Operador, Supervisor y Administrador tienen hoy los mismos permisos; el código solo distingue entre `residente` y "personal".

### Ciclo de vida de un caso

```
Registrado → En revisión → Derivado → Seguimiento → Cerrado
```
- `avanzar` mueve un paso a la vez.
- `derivar` salta directamente a `Derivado` (solo si aún no se alcanzó).
- Cada cambio genera un `Evento` en el historial.

---

## 7. Modelo de datos

```
usuarios (1) ──< casos.creadoPor      (sin FK declarada)
casos    (1) ──< eventos.casoId       (FK, ON DELETE CASCADE)
```

---

## 8. Observaciones y puntos de mejora

Hallazgos detectados al revisar el código, útiles para el informe o una futura iteración:

1. **`Session` es un objeto global en memoria**: se pierde si el proceso muere y el usuario vuelve a `login`. Una alternativa es persistir el id de usuario (DataStore) o inyectar la sesión.
2. **`allowBackup="true"`** en el manifest permite respaldar la base de datos (con hashes) en la nube de Google; considerar desactivarlo o definir reglas de respaldo.
3. **`CasoEntity.creadoPor` sin clave foránea** hacia `usuarios`; se podría declarar para reforzar la integridad.
4. **Sin migraciones Room** (`version = 1`, `exportSchema = false`). Para entregas posteriores conviene exportar el esquema y definir migraciones.
5. **PBKDF2 con 10 000 iteraciones**: válido para el MVP, pero las recomendaciones actuales son bastante superiores (cientos de miles) para SHA-256.
6. **Credenciales precargadas en `LoginScreen`** (correo y `demo1234`): útil para la demo, hay que quitarlo en una versión real.
7. **Mismos permisos para tres roles de personal**: si se quiere diferenciar (por ejemplo, que solo el Administrador gestione usuarios), `soloPersonal` debería evolucionar a una verificación por rol.
8. **Los datos no se comparten entre dispositivos**: cada celular tiene su propia base (limitación declarada en el README).
9. **`isMinifyEnabled = false`** en `release`; habilitar R8 y reglas ProGuard antes de publicar.
10. **Textos fijos en español dentro del código**; para internacionalizar habría que moverlos a `strings.xml`.
11. **Sin pruebas automatizadas**: la lógica del `Repositorio` (permisos y transiciones de estado) es buena candidata a pruebas unitarias.

---

## 9. Glosario rápido

| Término | Significado |
|---|---|
| Caso | Situación de seguridad comunal reportada. |
| Evento | Registro histórico de un cambio u observación en un caso. |
| Personal | Cualquier usuario cuyo rol no sea `residente`. |
| (sim.) | Dato simulado, usado en responsables y evidencias. |
| Sector (referencial) | Zona aproximada del caso; no usa geolocalización. |
