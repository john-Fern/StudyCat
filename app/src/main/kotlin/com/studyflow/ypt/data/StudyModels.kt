package com.studyflow.ypt.data

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme(val displayName: String, val description: String, val isDark: Boolean) {
  DARK_YPT("Noche Coral", "Oscuro clásico con acento coral", true),
  PASTEL_CAT("Gatito Sakura", "Tonos crema y rosa pastel kawaii", false),
  LIGHT_PAPER("Papel Estudio", "Claro minimalista para el día", false),
  MIDNIGHT_CYBER("Medianoche", "Azul profundo con neón cian", true),
  MATCHA_FOREST("Bosque Matcha", "Verde salvia relajante visual", true),
  LAVENDER_DREAM("Lavanda Suave", "Púrpura pastel relajante para estudiar", false),
  AMBER_SUNSET("Atardecer Ámbar", "Tono cálido dorado y café otoñal", true),
  NORDIC_FROST("Escarcha Nórdica", "Gris azulado frío y enfoque glacial", true),
  COFFEE_LATTE("Café Moca", "Tonos crema cálidos estilo cafetería", false),
  NEO_VIOLET("Galaxia Neón", "Púrpura espacial profundo y magenta", true),
  MINT_BREEZE("Brisa Menta", "Fresco verde menta matutino", false),
  CRIMSON_NIGHT("Noche Carmesí", "Elegante rojo vino y carbón profundo", true),
}

@Serializable
enum class TimerMode(val displayName: String) {
  STOPWATCH("Cronómetro"),
  POMODORO("Pomodoro"),
}

@Serializable
enum class PomodoroPhase(val displayName: String) {
  FOCUS("Enfoque"),
  SHORT_BREAK("Descanso Corto"),
  LONG_BREAK("Descanso Largo"),
}

@Serializable
data class PomodoroConfig(
  val focusMinutes: Int = 25,
  val shortBreakMinutes: Int = 5,
  val longBreakMinutes: Int = 15,
  val cyclesBeforeLongBreak: Int = 4,
  val alarmEnabled: Boolean = true,
)

@Serializable
data class Subject(
  val id: String,
  val name: String,
  val colorHex: Long,
  val targetMinutesPerDay: Int = 60,
)

@Serializable
data class StudySession(
  val id: String,
  val subjectId: String,
  val startTimeMillis: Long,
  val endTimeMillis: Long,
  val durationSeconds: Long,
  val dateKey: String, // YYYY-MM-DD
  val isPomodoro: Boolean = false,
)

@Serializable
data class PersistedStudyState(
  val subjects: List<Subject> = defaultSubjects(),
  val sessions: List<StudySession> = emptyList(),
  val dailyGoalMinutes: Int = 360, // 6 horas por defecto
  val selectedTheme: AppTheme = AppTheme.DARK_YPT,
  val preferredTimerMode: TimerMode = TimerMode.STOPWATCH,
  val pomodoroConfig: PomodoroConfig = PomodoroConfig(),
  val completedPomodorosByDate: Map<String, Int> = emptyMap(),
  val isHeaderCollapsed: Boolean = false,
)

fun defaultSubjects(): List<Subject> =
  listOf(
    Subject(id = "subj_1", name = "Ingeniería de Software", colorHex = 0xFFFF6B4A, targetMinutesPerDay = 120),
    Subject(id = "subj_2", name = "Algoritmos y Estructuras", colorHex = 0xFF3B82F6, targetMinutesPerDay = 90),
    Subject(id = "subj_3", name = "Kotlin & Android", colorHex = 0xFF10B981, targetMinutesPerDay = 90),
    Subject(id = "subj_4", name = "Inglés Técnico", colorHex = 0xFFA855F7, targetMinutesPerDay = 60),
  )

val SubjectColorPalette =
  listOf(
    // Rojos y Corales
    0xFFFF6B4A, // Coral Clásico
    0xFFEF4444, // Rojo Carmesí
    0xFFF43F5E, // Rosa Fresa
    0xFFBE123C, // Rubí Vino

    // Naranjas y Amarillos
    0xFFF97316, // Naranja Neón
    0xFFFB923C, // Melocotón
    0xFFF59E0B, // Ámbar Dorado
    0xFFEAB308, // Amarillo Mostaza

    // Verdes y Turquesas
    0xFF84CC16, // Lima
    0xFF22C55E, // Verde Vivo
    0xFF10B981, // Esmeralda
    0xFF059669, // Bosque Profundo
    0xFF14B8A6, // Turquesa
    0xFF06B6D4, // Cian Neón

    // Azules e Índigos
    0xFF0EA5E9, // Azul Celeste
    0xFF3B82F6, // Azul Real
    0xFF2563EB, // Azul Zafiro
    0xFF6366F1, // Índigo Eléctrico

    // Púrpuras y Rosas
    0xFF8B5CF6, // Lavanda
    0xFFA855F7, // Violeta
    0xFFD946EF, // Fucsia Neón
    0xFFEC4899, // Rosa Chicle
    0xFFF472B6, // Rosa Pastel

    // Terrosos y Neutros
    0xFF8D5B4C, // Café Moca
  )
