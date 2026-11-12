# StudyCat 🐱

Aplicación nativa de Android desarrollada en **Kotlin** y **Jetpack Compose** para el seguimiento y gestión de tiempos de estudio por materias, totalmente libre de anuncios y con almacenamiento 100% local y privado.

> ### 📲 [⬇️ TOCA AQUÍ PARA DESCARGAR StudyCat.apk DIRECTAMENTE EN TU CELULAR](https://github.com/john-Fern/StudyCat/raw/main/StudyCat.apk)

---

## ✨ Características Principales

1. **Gestión Completa de Materias y Bloques**:
   - Crea materias con metas diarias de estudio personalizadas.
   - Selección de colores distintivos por materia.
   - **Edición completa de materias**: Modifica el nombre, meta diaria en minutos y color en cualquier momento tocando la materia o el botón de edición.
   - **Edición de bloques estudiados**: Si iniciaste una sesión por error o necesitas corregir su finalización, puedes tocar cualquier bloque registrado para eliminarlo o ajustar su hora de término.

2. **Doble Modo de Temporizador**:
   - **Cronómetro Progresivo**: Cuenta progresiva por materia con guardado automático al pausar o detener.
   - **Temporizador Pomodoro**: Fases estructuradas de *Enfoque*, *Descanso Corto* y *Descanso Largo*. Configuración flexible de duración y ciclos.

3. **Herramientas de Enfoque**:
   - **Modo Pantalla Negra (OLED)**: Ahorro máximo de batería en pantallas AMOLED con visualización minimalista en fondo negro puro.
   - **Pantalla Siempre Activa (`KeepScreenOn`)**: Mantiene la pantalla encendida automáticamente mientras el temporizador o descanso está activo.

4. **Estadísticas Detalladas**:
   - Métricas diarias: Tiempo total de estudio, cantidad de sesiones, pomodoros completados y racha continua máxima.
   - **Timeline de 10 Minutos**: Cuadrícula de 24 horas dividida en bloques de 10 minutos que pinta la materia dominante en cada intervalo.
   - Gráfico de distribución porcentual por materia.

5. **Temas Visuales**:
   - *Noche Coral*: Tema oscuro con acentos en tono coral.
   - *Gatito Sakura*: Estilo pastel kawaii en tonos crema y rosa.
   - *Papel Estudio*: Tema claro minimalista para lectura diurna.
   - *Medianoche*: Fondo ultra oscuro con acentos en cian neón.
   - *Bosque Matcha*: Tonos verdes salvia para descanso visual.

6. **Privacidad y Modo Offline**:
   - Funciona sin conexión a internet y sin recolección de datos personales.
   - Almacenamiento local mediante serialización JSON (`kotlinx.serialization`).

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
