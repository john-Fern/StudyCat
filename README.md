# StudyCat 🐱

Aplicación nativa de Android desarrollada en **Kotlin** y **Jetpack Compose** para el seguimiento y gestión de tiempos de estudio por materias, totalmente libre de anuncios, con arquitectura **local-first** y respaldo opcional y seguro en la nube con **Supabase** (PostgreSQL + Row Level Security).

> ### 📲 [⬇️ TOCA AQUÍ PARA DESCARGAR StudyCat v1.4.0 DIRECTAMENTE EN TU CELULAR](https://github.com/john-Fern/StudyCat/raw/main/StudyCat.apk)

---

## ✨ Características Principales

1. **Gestión Completa de Materias y Bloques**:
   - Crea materias con metas diarias de estudio personalizadas y selección de colores distintivos.
   - **Edición completa de materias**: Modifica nombre, meta diaria y color en cualquier momento.
   - **Edición interactiva de bloques estudiados**: Corrige el tiempo de término o elimina cualquier bloque directamente desde la lista o tocando el bloque en el Timeline de 10 minutos.

2. **Doble Modo de Temporizador y Alarmas**:
   - **Cronómetro Progresivo**: Registro de tiempo por materia con guardado automático.
   - **Temporizador Pomodoro**: Fases de *Enfoque*, *Descanso Corto* y *Descanso Largo*.
   - **Alarmas Automáticas**: Notificación visual en pantalla, tono en canal de alarma del sistema y vibración continua al finalizar el tiempo de enfoque o descanso.

3. **Respaldo en la Nube y Sincronización Segura**:
   - Inicia sesión con **Google** (OAuth 2.0 con PKCE) o con **Correo y Contraseña**.
   - Respaldo automático debounced en **Supabase** (PostgreSQL).
   - **Máxima Seguridad**: Políticas de Row Level Security (RLS) estrictas (`auth.uid() = user_id`) y almacenamiento de tokens cifrado por hardware en el dispositivo con **AndroidKeyStore** (AES-256-GCM).
   - **Local-first**: Tus datos nunca se pierden; si no hay internet o cierras sesión, tu información permanece intacta en el teléfono.
   - **Derecho al olvido**: Botón para eliminar tu respaldo de la nube en cualquier momento.

4. **Herramientas de Enfoque**:
   - **Modo Pantalla Negra (OLED)**: Ahorro máximo de batería en pantallas AMOLED con fondo `#000000`.
   - **Pantalla Siempre Activa (`KeepScreenOn`)**: Mantiene la pantalla encendida automáticamente mientras estudias o descansas.

5. **Estadísticas Detalladas**:
   - Métricas diarias: Tiempo total de estudio, cantidad de sesiones, pomodoros completados y racha continua máxima.
   - **Timeline Interactivo de 10 Minutos**: Visualización de 24 horas dividida en bloques de 10 minutos con acceso táctil al editor de bloques.
   - Gráfico de distribución porcentual por materia.

6. **5 Temas Visuales**:
   - *Noche Coral*: Tema oscuro clásico con acento coral.
   - *Gatito Sakura*: Estilo pastel kawaii en tonos crema y rosa.
   - *Papel Estudio*: Tema claro minimalista para lectura diurna.
   - *Medianoche*: Fondo ultra oscuro con acentos en cian neón.
   - *Bosque Matcha*: Tonos verdes salvia para descanso visual.

---

## 📱 Descarga e Instalación

Puedes descargar e instalar la aplicación en tu teléfono Android de dos formas:

1. **Descarga directa en 1 toque (Ideal para celular):**
   - Toca este enlace directo: [**⬇️ Descargar StudyCat.apk**](https://github.com/john-Fern/StudyCat/raw/main/StudyCat.apk)
2. **Desde GitHub Releases:**
   - Ve a la sección de [Releases de StudyCat](https://github.com/john-Fern/StudyCat/releases), despliega **Assets** y toca `StudyCat.apk`.

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje:** Kotlin 2.3.20 (JVM Toolchain 17)
- **UI:** Jetpack Compose (BOM 2026.03.01) + Material 3
- **Navegación:** AndroidX Navigation 3 (`androidx.navigation3`)
- **Persistencia:** SharedPreferences + `kotlinx.serialization-json`
- **Arquitectura:** MVVM ligera con Unidirectional Data Flow (UDF)
- **Testing:** JUnit + Kotlinx Coroutines Test

---

## 🚀 Compilación y Pruebas

Para compilar el proyecto localmente con Gradle:

```bash
# Ejecutar tests unitarios
./gradlew testDebugUnitTest

# Generar APK de depuración
./gradlew assembleDebug
```

El APK generado se encontrará en `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
