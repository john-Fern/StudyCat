package com.studyflow.ypt.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface DataRepository {
  val data: Flow<List<String>>
  val studyState: Flow<PersistedStudyState>
    get() = MutableStateFlow(PersistedStudyState())
  val currentStudyState: PersistedStudyState
    get() = PersistedStudyState()

  fun addSubject(name: String, colorHex: Long, targetMinutesPerDay: Int = 60) {}

  fun updateSubject(
    subjectId: String,
    name: String,
    colorHex: Long,
    targetMinutesPerDay: Int = 60,
  ) {}

  fun deleteSubject(subjectId: String) {}

  fun recordSession(
    subjectId: String,
    startTimeMillis: Long,
    endTimeMillis: Long,
    durationSeconds: Long,
    isPomodoro: Boolean = false,
  ) {}

  fun updateSessionEndTime(sessionId: String, newEndTimeMillis: Long) {}

  fun deleteSession(sessionId: String) {}

  fun updateDailyGoal(minutes: Int) {}

  fun updateTheme(theme: AppTheme) {}

  fun updateTimerMode(mode: TimerMode) {}

  fun updatePomodoroConfig(
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int = 15,
    cyclesBeforeLongBreak: Int = 4,
    alarmEnabled: Boolean = true,
  ) {}

  fun incrementCompletedPomodoros(dateKey: String? = null) {}
}

class DefaultDataRepository(context: Context? = null) : DataRepository {
  private val prefs = context?.getSharedPreferences("studyflow_ypt_prefs", Context.MODE_PRIVATE)
  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  private val _studyState = MutableStateFlow(loadInitialState())
  override val studyState: Flow<PersistedStudyState> = _studyState.asStateFlow()
  override val currentStudyState: PersistedStudyState
    get() = _studyState.value

  override val data: Flow<List<String>> = _studyState.map { state -> state.subjects.map { it.name } }

  private fun loadInitialState(): PersistedStudyState {
    val raw = prefs?.getString("persisted_study_state", null) ?: return PersistedStudyState()
    return try {
      json.decodeFromString<PersistedStudyState>(raw)
    } catch (_: Exception) {
      PersistedStudyState()
    }
  }

  private fun saveState(newState: PersistedStudyState) {
    _studyState.value = newState
    prefs?.edit()?.putString("persisted_study_state", json.encodeToString(newState))?.apply()
  }

  override fun addSubject(name: String, colorHex: Long, targetMinutesPerDay: Int) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return
    val newSubject =
      Subject(
        id = "subj_${UUID.randomUUID()}",
        name = trimmed,
        colorHex = colorHex,
        targetMinutesPerDay = targetMinutesPerDay.coerceIn(5, 720),
      )
    val current = _studyState.value
    saveState(current.copy(subjects = current.subjects + newSubject))
  }

  override fun updateSubject(
    subjectId: String,
    name: String,
    colorHex: Long,
    targetMinutesPerDay: Int,
  ) {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return
    val current = _studyState.value
    val updatedSubjects =
      current.subjects.map { subject ->
        if (subject.id == subjectId) {
          subject.copy(
            name = trimmed,
            colorHex = colorHex,
            targetMinutesPerDay = targetMinutesPerDay.coerceIn(5, 720),
          )
        } else {
          subject
        }
      }
    saveState(current.copy(subjects = updatedSubjects))
  }

  override fun deleteSubject(subjectId: String) {
    val current = _studyState.value
    saveState(
      current.copy(
        subjects = current.subjects.filterNot { it.id == subjectId },
        sessions = current.sessions.filterNot { it.subjectId == subjectId },
      )
    )
  }

  override fun recordSession(
    subjectId: String,
    startTimeMillis: Long,
    endTimeMillis: Long,
    durationSeconds: Long,
    isPomodoro: Boolean,
  ) {
    if (durationSeconds <= 0L) return
    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(startTimeMillis))
    val newSession =
      StudySession(
        id = "sess_${UUID.randomUUID()}",
        subjectId = subjectId,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        durationSeconds = durationSeconds,
        dateKey = dateKey,
        isPomodoro = isPomodoro,
      )
    val current = _studyState.value
    saveState(current.copy(sessions = current.sessions + newSession))
  }

  override fun updateSessionEndTime(sessionId: String, newEndTimeMillis: Long) {
    val current = _studyState.value
    val updatedSessions =
      current.sessions.map { session ->
        if (session.id == sessionId) {
          val validEndMillis = newEndTimeMillis.coerceAtLeast(session.startTimeMillis + 1_000L)
          val newDurationSeconds =
            ((validEndMillis - session.startTimeMillis) / 1_000L).coerceAtLeast(1L)
          session.copy(
            endTimeMillis = session.startTimeMillis + newDurationSeconds * 1_000L,
            durationSeconds = newDurationSeconds,
          )
        } else {
          session
        }
      }
    saveState(current.copy(sessions = updatedSessions))
  }

  override fun deleteSession(sessionId: String) {
    val current = _studyState.value
    val target = current.sessions.find { it.id == sessionId } ?: return
    val updatedPomodoros = current.completedPomodorosByDate.toMutableMap()
    if (target.isPomodoro) {
      val currentCount = updatedPomodoros[target.dateKey] ?: 0
      if (currentCount > 0) {
        updatedPomodoros[target.dateKey] = currentCount - 1
      }
    }
    saveState(
      current.copy(
        sessions = current.sessions.filterNot { it.id == sessionId },
        completedPomodorosByDate = updatedPomodoros,
      )
    )
  }

  override fun updateDailyGoal(minutes: Int) {
    val current = _studyState.value
    saveState(current.copy(dailyGoalMinutes = minutes.coerceIn(30, 16 * 60)))
  }

  override fun updateTheme(theme: AppTheme) {
    val current = _studyState.value
    saveState(current.copy(selectedTheme = theme))
  }

  override fun updateTimerMode(mode: TimerMode) {
    val current = _studyState.value
    saveState(current.copy(preferredTimerMode = mode))
  }

  override fun updatePomodoroConfig(
    focusMinutes: Int,
    shortBreakMinutes: Int,
    longBreakMinutes: Int,
    cyclesBeforeLongBreak: Int,
    alarmEnabled: Boolean,
  ) {
    val current = _studyState.value
    val newConfig =
      PomodoroConfig(
        focusMinutes = focusMinutes.coerceIn(1, 180),
        shortBreakMinutes = shortBreakMinutes.coerceIn(1, 60),
        longBreakMinutes = longBreakMinutes.coerceIn(1, 90),
        cyclesBeforeLongBreak = cyclesBeforeLongBreak.coerceIn(1, 12),
        alarmEnabled = alarmEnabled,
      )
    saveState(current.copy(pomodoroConfig = newConfig))
  }

  override fun incrementCompletedPomodoros(dateKey: String?) {
    val key = dateKey ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val current = _studyState.value
    val updatedMap = current.completedPomodorosByDate.toMutableMap()
    updatedMap[key] = (updatedMap[key] ?: 0) + 1
    saveState(current.copy(completedPomodorosByDate = updatedMap))
  }
}
