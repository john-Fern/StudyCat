package com.studyflow.ypt.data

import kotlinx.serialization.Serializable

@Serializable
enum class AppTheme(val displayName: String, val description: String, val isDark: Boolean) {
  DARK_YPT("Noche YPT", "Oscuro clásico con acento coral", true),
  PASTEL_CAT("Gatito Sakura", "Tonos crema y rosa pastel kawaii", false),
  LIGHT_PAPER("Papel Estudio", "Claro minimalista para el día", false),
  MIDNIGHT_CYBER("Medianoche", "Azul profundo con neón cian", true),
  MATCHA_FOREST("Bosque Matcha", "Verde salvia relajante visual", true),
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
    0xFFFF6B4A, // Coral YPT
    0xFF3B82F6, // Azul
    0xFF10B981, // Esmeralda
    0xFFA855F7, // Violeta
    0xFFF59E0B, // Ámbar
    0xFFEC4899, // Rosa
    0xFF06B6D4, // Cian
    0xFF84CC16, // Lima
  )
