# StudyCat — Guía para Agentes (AGENTS.md)

**StudyCat** es una aplicación nativa de Android para gestionar tiempos de estudio por materias, 100% libre de anuncios y con almacenamiento 100% local/offline. Combina **Cronómetro progresivo por materia**, **Temporizador Pomodoro por fases**, **Modo de Enfoque OLED**, **5 temas visuales** (`StudyCatPalette`) y **estadísticas diarias detalladas** (incluyendo la cuadrícula de bloques de 10 minutos).

Antes de modificar cualquier archivo, respeta las reglas globales de XP/KISS/TDD y las convenciones específicas de esta arquitectura.

---

## 1. Stack Tecnológico y Configuración

- **Lenguaje y SDK:** Kotlin `2.3.20` (JVM Toolchain 17), Android Gradle Plugin `9.0.1`, `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`.
- **UI Declarativa:** Jetpack Compose (BOM `2026.03.01`), Material 3, `material-icons-extended`.
- **Navegación:** `androidx.navigation3` (`NavDisplay`, `rememberNavBackStack`, `entryProvider` en `Navigation.kt` y `NavigationKeys.kt`).
- **Persistencia Local:** `SharedPreferences` (`studyflow_ypt_prefs`) + `kotlinx-serialization-json` (`1.8.0`). No agregues servidores remotos, trackers ni SDKs de terceros innecesarios.
- **Gestión de Dependencias:** Centralizada exclusivamente en `gradle/libs.versions.toml`.

---

## 2. Arquitectura y Patrones del Proyecto

El proyecto sigue una arquitectura **MVVM ligera con Flujo de Datos Unidireccional (UDF)**, diseñada para ser simple (KISS/YAGNI) y 100% testeable sin frameworks pesados de inyección de dependencias:

### Capa de Datos (`com.studyflow.ypt.data`)
- **`StudyModels.kt`:** Contiene todos los modelos serializables (`@Serializable`): `AppTheme`, `TimerMode`, `PomodoroPhase`, `PomodoroConfig`, `Subject`, `StudySession` y el estado raíz `PersistedStudyState`.
  - **Regla de retrocompatibilidad JSON:** Cada vez que agregues una propiedad nueva a `PersistedStudyState` o a un modelo persistido, **debe tener un valor por defecto** (`= ...`). `DefaultDataRepository` usa `Json { ignoreUnknownKeys = true; encodeDefaults = true }` para no romper los datos ya guardados del usuario.
- **`DataRepository.kt`:** Interfaz `DataRepository` e implementación `DefaultDataRepository(context: Context? = null)`.
  - Cuando `context == null`, funciona en memoria sin tocar disco, lo que permite usar `DefaultDataRepository(context = null)` directamente en tests unitarios rápidos.

### Capa de UI y Estado (`com.studyflow.ypt.ui`)
- **`MainScreenViewModel.kt`:**
  - Recibe por constructor `dataRepository: DataRepository`, `currentTimeProvider: () -> Long = { System.currentTimeMillis() }` y `coroutineScope: CoroutineScope? = null`.
  - **Regla de determinismo temporal:** Nunca llames a `System.currentTimeMillis()` ni `Date()` directamente dentro de la lógica del ViewModel; usa siempre `currentTimeProvider()` para que los tests de corrutinas puedan simular el paso del tiempo con `testScheduler.currentTime`.
  - Expone `yptState: StateFlow<YptDashboardState>` combinando el estado persistido (`dataRepository.studyState`) con el estado en vivo del temporizador (`_activeTimer`).
- **`MainScreen.kt` y Temas (`StudyCatPalette`):**
  - El sistema de 5 temas (`DARK_YPT`, `PASTEL_CAT`, `LIGHT_PAPER`, `MIDNIGHT_CYBER`, `MATCHA_FOREST`) se resuelve mediante `paletteForTheme(theme)` y se inyecta por `LocalPalette` (`CompositionLocalProvider`).
  - Todo nuevo componente visual debe usar los colores de `LocalPalette.current` (`palette.bg`, `palette.cardBg`, `palette.accent`, `palette.textPrimary`, `palette.textSecondary`, `palette.subtleSurface`) en lugar de colores hardcodeados, para que se vea bien en los 5 temas (claros y oscuros).
- **`KeepScreenOn.kt`:**
  - Mantiene la pantalla encendida (`View.keepScreenOn` + `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`) siempre que `activeTimer.isRunning` sea `true` (tanto en estudio como en descanso o modo OLED). No elimines este comportamiento al tocar la pantalla del temporizador.

---

## 3. Reglas de Negocio Invariantes

1. **Registro de sesiones:** Tanto en modo Cronómetro como al detener o completar una fase `FOCUS` de Pomodoro (si la duración es `>= 1` segundo), la sesión se guarda automáticamente en `DataRepository.recordSession(...)`. Los descansos (`SHORT_BREAK` / `LONG_BREAK`) no suman tiempo de estudio a las materias.
2. **Cuadrícula de 10 minutos (`tenMinuteBlocks`):** Divide las 24 horas del día (`0..23`) en 6 bloques de 10 minutos cada una (`0..5`), pintando tanto las sesiones guardadas del día como la sesión activa en curso (`id = "live"`).
3. **Modo Pantalla Negra (OLED):** Solo puede activarse cuando `activeTimer.isRunning == true` y permite ahorrar batería en pantallas AMOLED mostrando únicamente información mínima sobre fondo `#000000` puro.
4. **Idioma de la interfaz:** Todos los textos visibles para el usuario deben estar en **español** claro y consistente con la identidad de **StudyCat**.

---

## 4. Flujo TDD y Verificación CI/CD Obligatoria

Para cada nueva funcionalidad o corrección de bug:
1. **Escribe o actualiza primero el test unitario** en `app/src/test/kotlin/com/studyflow/ypt/ui/main/MainScreenViewModelTest.kt` (usando `runTest`, `UnconfinedTestDispatcher(testScheduler)`, `advanceTimeBy` y `currentTimeProvider = { baseTime + testScheduler.currentTime }`).
2. **Implementa el cambio mínimo** manteniendo el código limpio y legible.
3. **Ejecuta la verificación obligatoria** antes de dar la tarea por terminada:

```powershell
# 1. Ejecutar tests unitarios
.\gradlew.bat testDebugUnitTest

# 2. Verificar compilación y ensamblado del APK debug
.\gradlew.bat assembleDebug
```

*(Opcional: Si el usuario solicita actualizar el APK raíz listo para instalar, copia `app\build\outputs\apk\debug\app-debug.apk` hacia `StudyCat.apk` en la raíz del proyecto).*
