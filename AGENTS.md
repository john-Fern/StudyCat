# StudyCat — Guía para Agentes (AGENTS.md)

**StudyCat** es una aplicación nativa de Android para gestionar tiempos de estudio por materias, 100% libre de anuncios y con arquitectura **local-first** y respaldo opcional en la nube con **Supabase** (PostgreSQL + RLS). Combina **Cronómetro progresivo por materia**, **Temporizador Pomodoro por fases con alarmas sonoras/vibración**, **Modo de Enfoque OLED**, **12 temas visuales** (`StudyCatPalette` para modos oscuros y claros), **paleta extendida de 24 colores para materias**, **modo minimalista con cabecera comprimible**, **estadísticas diarias detalladas** (incluyendo la cuadrícula interactiva de bloques de 10 minutos) y **sincronización segura en la nube (Email/Password y Google OAuth con PKCE)**.

Antes de modificar cualquier archivo, respeta las reglas globales de XP/KISS/TDD y las convenciones específicas de esta arquitectura.

---

## 1. Stack Tecnológico y Configuración

- **Lenguaje y SDK:** Kotlin `2.3.20` (JVM Toolchain 17), Android Gradle Plugin `9.0.1`, `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`.
- **UI Declarativa:** Jetpack Compose (BOM `2026.03.01`), Material 3, `material-icons-extended`.
- **Navegación y Enlaces Profundos:** `androidx.navigation3` (`NavDisplay`, `rememberNavBackStack`, `entryProvider` en `Navigation.kt` y `NavigationKeys.kt`). Deep link `studycat://auth-callback` para retornos de OAuth 2.0.
- **Persistencia Local:** `SharedPreferences` (`studyflow_ypt_prefs`) + `kotlinx-serialization-json` (`1.8.0`). La aplicación es 100% funcional sin conexión.
- **Seguridad y Cifrado:** `AndroidKeyStore` con AES-256-GCM para almacenar tokens de autenticación en reposo en `studycat_secure_auth.xml` (excluido de backups automáticos del sistema).
- **Backend en la Nube:** Supabase (PostgreSQL 17 en `sa-east-1`, tabla `public.user_study_backups` con Row Level Security estricto). No requiere servidores intermedios costosos ni pesados SDKs adicionales; utiliza HTTP nativo y serialización Kotlin estándar.
- **Gestión de Dependencias:** Centralizada exclusivamente en `gradle/libs.versions.toml`.

---

## 2. Arquitectura y Patrones del Proyecto

El proyecto sigue una arquitectura **MVVM ligera con Flujo de Datos Unidireccional (UDF)**, diseñada para ser simple (KISS/YAGNI) y 100% testeable sin frameworks pesados de inyección de dependencias:

### Capa de Datos (`com.studyflow.ypt.data`)
- **`StudyModels.kt`:** Modelos serializables (`@Serializable`): `AppTheme`, `TimerMode`, `PomodoroPhase`, `PomodoroConfig`, `Subject`, `StudySession` y `PersistedStudyState`.
  - **Regla de retrocompatibilidad JSON:** Cada vez que agregues una propiedad nueva a `PersistedStudyState` o a un modelo persistido, **debe tener un valor por defecto** (`= ...`). `DefaultDataRepository` usa `Json { ignoreUnknownKeys = true; encodeDefaults = true }` para no romper los datos ya guardados del usuario.
- **`DataRepository.kt`:** Interfaz `DataRepository` e implementación `DefaultDataRepository(context: Context? = null)`.
  - Cuando `context == null`, funciona en memoria sin tocar disco, lo que permite usarlo directamente en tests unitarios rápidos.
  - Expone `replaceState(newState: PersistedStudyState)` para aplicar fusiones seguras provenientes de la nube.
- **Capa Cloud (`com.studyflow.ypt.data.cloud`):**
  - `CloudSyncLogic.kt`: Lógica pura de fusión (`mergeStudyStates`), saneamiento en fronteras (`sanitizeStudyState`), validación de correo (`isValidEmailInput`) y criptografía PKCE (`generatePkceVerifier`, `pkceCodeChallenge`, `parseCallbackParams`). Totalmente desacoplada de Android para pruebas unitarias instantáneas en la JVM.
  - `CloudSyncService.kt`: Interfaz `CloudSyncService` e implementación `SupabaseCloudSyncService` conectada a Supabase Auth y PostgREST con rotación de tokens y cifrado `AndroidKeyStore`.

### Capa de UI y Estado (`com.studyflow.ypt.ui`)
- **`MainScreenViewModel.kt`:**
  - Recibe por constructor `dataRepository: DataRepository`, `currentTimeProvider: () -> Long`, `coroutineScope: CoroutineScope? = null` y `cloudSyncService: CloudSyncService? = null`.
  - **Regla de determinismo temporal:** Nunca llames a `System.currentTimeMillis()` ni `Date()` directamente dentro de la lógica del ViewModel; usa siempre `currentTimeProvider()` para que los tests de corrutinas puedan simular el paso del tiempo con `testScheduler.currentTime`.
  - Expone `yptState: StateFlow<YptDashboardState>` y `cloudAccountState: StateFlow<CloudAccountUiState>`.
  - Realiza sincronización automática debounced (`AUTO_SYNC_DEBOUNCE_MILLIS = 3_000L`) ante cambios locales mientras el usuario tenga sesión activa.
- **`MainScreen.kt` y Temas (`StudyCatPalette`):**
  - 12 temas (`DARK_YPT`, `PASTEL_CAT`, `LIGHT_PAPER`, `MIDNIGHT_CYBER`, `MATCHA_FOREST`, `LAVENDER_DREAM`, `AMBER_SUNSET`, `NORDIC_FROST`, `COFFEE_LATTE`, `NEO_VIOLET`, `MINT_BREEZE`, `CRIMSON_NIGHT`) resueltos por `paletteForTheme(theme)` e inyectados por `LocalPalette`.
  - Paleta de 24 colores para materias en cuadrícula interactiva con vista previa en tiempo real (`SubjectColorPalette`, `SubjectColorGrid`).
  - Todo nuevo componente visual debe usar `LocalPalette.current` (`palette.bg`, `palette.cardBg`, `palette.accent`, `palette.textPrimary`, `palette.textSecondary`, `palette.subtleSurface`).
- **`KeepScreenOn.kt`:**
  - Mantiene la pantalla encendida siempre que `activeTimer.isRunning` o `activeAlarm != null`. Maneja además `PomodoroAlarmEffect` (audio en canal de alarma + vibración rítmica).

---

## 3. Reglas de Negocio y Seguridad Invariantes

1. **Local-first y Preservación de Datos:** Los datos del usuario NUNCA se borran del dispositivo al cerrar sesión en la nube ni al fallar la red. La app continúa funcionando al 100% offline.
2. **Seguridad por Defecto en la Nube:**
   - Nunca expongas la clave `service_role` ni tokens con privilegios de administrador en el cliente. Solo se utiliza la clave `publishable` de Supabase.
   - En PostgreSQL, la tabla `user_study_backups` tiene Row Level Security forzado (`force row level security`), revocados todos los accesos al rol `anon`, y políticas estrictas para `authenticated` usando `(select auth.uid()) = user_id`.
   - Google OAuth utiliza flujo de autorización con PKCE (RFC 7636). El `code_verifier` se almacena cifrado en el dispositivo y se elimina inmediatamente tras el canje.
3. **Registro y Edición de Sesiones:** Tanto en Cronómetro como al completar Enfoque Pomodoro, las sesiones `>= 1` segundo se guardan automáticamente. Tocar cualquier bloque en el Timeline interactivo de 10 minutos abre el diálogo para ajustar el tiempo de término o borrarlo.
4. **Alarmas de Pomodoro:** Disparan aviso visual, tono en canal de alarma del sistema y vibración continua tanto al terminar un Pomodoro (`FOCUS_COMPLETED`) como al terminar un descanso (`BREAK_COMPLETED`).
5. **Idioma de la interfaz:** Todos los textos visibles para el usuario deben estar en **español** claro y consistente con la identidad de **StudyCat**.

---

## 4. Flujo TDD y Verificación CI/CD Obligatoria

Para cada nueva funcionalidad o corrección de bug:
1. **Escribe o actualiza primero los tests unitarios** en:
   - `app/src/test/kotlin/com/studyflow/ypt/ui/main/MainScreenViewModelTest.kt`
   - `app/src/test/kotlin/com/studyflow/ypt/ui/main/CloudAccountViewModelTest.kt`
   - `app/src/test/kotlin/com/studyflow/ypt/data/cloud/CloudSyncLogicTest.kt`
2. **Implementa el cambio mínimo** manteniendo el código limpio y legible.
3. **Ejecuta la verificación obligatoria** antes de dar la tarea por terminada:

```powershell
# 1. Ejecutar tests unitarios
.\gradlew.bat testDebugUnitTest

# 2. Verificar compilación y ensamblado del APK debug
.\gradlew.bat assembleDebug
```

*(Opcional: Si el usuario solicita actualizar el APK raíz listo para instalar, copia `app\build\outputs\apk\debug\app-debug.apk` hacia `StudyCat.apk` en la raíz del proyecto).*
